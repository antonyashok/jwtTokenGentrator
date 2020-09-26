function clusteringSankey(id, cluterIndex) {

    const mode = id;
    const margin = {top: 100, right: 50, bottom: 100, left: 50};
    const width =  document.getElementById("diagramwidth").value - margin.left - margin.right;
    const height = 1300;

// this is to build a svg canvas
    const svg = d3.select(id)
        .attr('width', width + margin.left + margin.right)
        .attr('height', height + margin.top + margin.bottom)
        .append('g')
        .attr('transform', 'translate(' + margin.left + ',' + margin.top + ')');

    const formatAsPercent4dp = d3.format('.4%'); // let javascript display numbers the day expected
    const formatAsFloat4dp = d3.format('.4f');
    const defaultMode = "countTraversals";
    const linkUnit = defaultMode == "countTraversals" ? "times" : (defaultMode == "countStudents" ? "students" : "");

    const color = d3.scaleOrdinal(d3.schemeCategory20);
    let inputData = null; //last data received from server



    // document.querySelector('#grad2 stop').setAttribute('stop-opacity',
    //     document.getElementById('Leave-link').defaultValue);

    document.getElementById('chart-title').textContent = localStorage.getItem("courseName");

    var defaultFilters = {graphtype: 'VDSankey', mode: defaultMode, clustering: cluterIndex};
    var filters = Object.assign({}, defaultFilters);

// this is to set the layout of the sankey diagram
    var sankey = d3.sankey()
        .nodeWidth(10) // means the node width
        .nodePadding(150); // means the vertical distance of  two rectangle

    var path = sankey.link();

    d3.json('/')
        .header('content-type', 'application/json')
        .post(JSON.stringify(filters), function (error, data) {
            if (error) throw error;
            drawSankey(data);
        })


    var drawSankey = function (data) {
        inputData = data;

        var minLinkSize = document.getElementById("minLinkSizeInput");

        minLinkSize = minLinkSize.validity.valid ? parseInt(minLinkSize.value, 10) : 25;


        sankey.nodes(data.nodes)
            .links(data.links)
            .size([width, height])
            .layout(minLinkSize);

        console.log("path "+ path);
        var links = svg.append('g').selectAll('.link')
            .data(sankey.links())
            .enter().append('g')
            .attr('class', 'link')
            .attr('d', path)
            .classed('backward', d => d.target.x < d.source.x)
            //TODO: do the +10px threshold a different way?
            .classed('jump', d => Math.abs(d.target.x - d.source.x) > sankey.nodePitchX() + 10)
            .classed('leave', d => d.target === sankey.nodes()[sankey.nodes().length - 1])
            .sort((a, b) => b.dy - a.dy)
            .on('dblclick', function (d) {
                filters.link = {
                    source: sankey.nodes().indexOf(d.source),
                    target: sankey.nodes().indexOf(d.target)
                };
                d3.json('/')
                    .header('content-type', 'application/json')
                    .post(JSON.stringify(filters), function (error, data) {
                        if (error) throw error;
                        svg.selectAll('*').remove();
                        drawSankey(data);
                    });
            });


        //for each link to last node, append 3 paths
        d3.selectAll('.link.leave').each(function (d) {
            var thisLink = d3.select(this);
            var pathStrings = path(d);  //should return array of 3 strings
            thisLink.append('path')
                .attr('d', pathStrings[0])
                //no data attached to path elements, so .link data is used instead?
                .style('stroke-width', d => Math.max(1, d.dy));

            thisLink.append('path')
                .attr('d', pathStrings[1])
                .style('stroke-width', d => Math.max(1, d.dy));

            thisLink.append('path')
                .attr('d', pathStrings[2])
                .style('stroke-width', d => Math.max(1, d.dy - 1));//-1 to make links entering last node distinguishable
        });


        //for all other links, append one path
        d3.selectAll('.link:not(.leave)').append('path')
            .attr('d', d => path(d))  //path(d) should return a string
            .style('stroke-width', d => Math.max(1, d.dy));

        links.append('title')
            .text(function (d) {
                if (d.pValue != null) { //if a chi squared test was run on this link
                    if (filters.mode === 'countTraversals') {
                        return d.source.name + ' \u2192 ' + d.target.name + '\n' + d.value + ' ' + linkUnit
                            + '\n(Double-click to filter for students who traversed this link.)'
                            + '\npValue = ' + formatAsPercent4dp(d.pValue)
                            + '\nexpected in set = ' + formatAsFloat4dp(d.expectedInSet)
                            + '\nexpected out of set = ' + formatAsFloat4dp(d.expectedOutOfSet)
                            + '\nobserved in set = ' + d.observedInSet
                            + '\nobserved out of set = ' + d.observedOutOfSet
                            + '\nStudents in the set produce ' + (d.observedInSet > d.expectedInSet ? 'MORE' : 'LESS')
                            + ' traversals than expected.';
                    }
                    else if (filters.mode === 'countStudents') {
                        return d.source.name + ' \u2192 ' + d.target.name + '\n' + d.value + ' ' + linkUnit
                            + '\n(Double-click to filter for students who traversed this link.)'
                            + '\npValue = ' + formatAsPercent4dp(d.pValue)
                            + '\n\u03c6 = ' + formatAsFloat4dp(d.phi)
                            + '\nin set and on link = ' + d.inSetAndOnLink
                            + '\nin set and off link = ' + d.inSetAndOffLink
                            + '\nout of set and on link = ' + d.outOfSetAndOnLink
                            + '\nout of set and off link = ' + d.outOfSetAndOffLink
                            + '\nStudents in the set are '
                            + (d.inSetAndOnLink / d.inSetAndOffLink > d.outOfSetAndOnLink / d.outOfSetAndOffLink ? 'MORE' : 'LESS')
                            + ' likely to be on this link.';
                    }
                }
                else { //if no chi squared test was run on this link
                    return d.source.name + ' \u2192 ' + d.target.name + '\n' + d.value + ' ' + linkUnit
                        + '\npValue not calculated. Expected frequencies too low.'
                        + '\n(Double-click to filter for students who traversed this link.)';
                }
            });

        var movingLinks = null; //temp variable for selection of links being dragged with node

        var nodes = svg.append('g').selectAll('.node')
            .data(sankey.nodes()) //array of nodes with small links filtered out
            .enter().append('g')
            .attr('class', 'node')
            .attr('transform', d => 'translate(' + d.x + ',' + d.y + ')')
            .on('dblclick', function (d, i) {
                filters.node = i;
                d3.json('/')
                    .header('content-type', 'application/json')
                    .post(JSON.stringify(filters), function (error, data) {
                        if (error) throw error;
                        svg.selectAll('*').remove();
                        drawSankey(data);
                    });
            })
            .call(d3.drag()
                .on('start', function (dMovingNode) {
                    //dim all links not connected to node being dragged
                    d3.selectAll('.link')
                        .classed('dimmed', dLink => dLink.source != dMovingNode && dLink.target != dMovingNode);

                    var touchingNodesData = new Set();  //set of data of nodes touching moving links
                    //select the moving links and add data of all nodes touching them to set
                    movingLinks = d3.selectAll('.link:not(.dimmed)')
                        .each(function (dLink) {
                            touchingNodesData.add(dLink.source);
                            touchingNodesData.add(dLink.target);
                        });
                    //dim all nodes which are not touching the moving links
                    d3.selectAll('.node')
                        .classed('dimmed', dNode => !touchingNodesData.has(dNode));
                })
                .on('drag', function (d) {
                    d3.select(this).attr('transform', 'translate(' + (d.x = d3.event.x) + ',' + (d.y = d3.event.y) + ')');

                    movingLinks.each(function (d) {
                        var r = path(d);
                        if (r instanceof Array) {
                            d3.select(this).selectAll('path')
                                .attr('d', (d, i) => r[i]);
                        }
                        else {
                            d3.select(this).select('path')
                                .attr('d', r);
                        }
                    });
                })
                .on('end', function () {
                    //un-dim all links and nodes
                    d3.selectAll('.link, .node')
                        .classed('dimmed', false);

                    sankey.relayout();
                    links.each(function (d) {
                        var thisLink = d3.select(this);
                        var paths = path(d);  //returns a path string OR array of 3 path strings

                        if (paths instanceof Array) {
                            thisLink.select('path:nth-of-type(1)')
                                .attr('d', paths[0]);
                            thisLink.select('path:nth-of-type(2)')
                                .attr('d', paths[1]);
                            thisLink.select('path:nth-of-type(3)')
                                .attr('d', paths[2]);
                        }
                        else {
                            thisLink.select('path')
                                .attr('d', paths);
                        }
                    });
                })
            );

        var nonZeroNodes = nodes.filter(d => d.value > 0);


        //append 1 rect to node if trueValue is not present, else, append 2 rects
        nonZeroNodes.each(function (d) {
            var thisNode = d3.select(this);

            if (d.trueValue === undefined) {
                thisNode.append('rect')
                    .attr('height', d => d.dy)
                    .attr('width', sankey.nodeWidth())
                    .style('fill', d => d.color = color(d.name.replace(/ .*/, '')))
                    .style('stroke', d => d3.rgb(d.color).darker(2))
                    .classed("discussionNode", d => d.isDiscussion != undefined && d.isDiscussion == 1)
                    .append('title')
                    .text(function (d) {
                        if (d.post != undefined && d.comment != undefined) {
                            return d.name + '\n' + d.value + ' ' + linkUnit
                                + '\n' + 'Number of Post: ' + d.post
                                + '\n' + 'Number of Comment: ' + d.comment
                                + '\n' + 'Number of PostVote: ' + d.postVote
                                + '\n' + 'Number of Response: ' + d.response
                                + '\n' + 'Number of ResponseVote: ' + d.responseVote
                                + '\n(Double-click to filter for students who passed this node.)' + '\n' + d.v1;
                        } else {

                            return d.name + '\n' + d.value + ' ' + linkUnit
                                + '\n(Double-click to filter for students who passed this node.)' + '\n' + d.v1;
                        }
                    });
            }
            else {
                thisNode.append('rect')//bigger rect
                    .attr('height', d => d.dy)
                    .attr('width', sankey.nodeWidth())
                    .style('fill', function (d) {
                        d.lightColor = color(d.name.replace(/ .*/, ''))
                        d.lightColor_ = d3.hsl(d.lightColor);
                        d.lightColor_.s -= 0.4 * d.lightColor_.s;
                        d.lightColor_.l += 0.7 * (1 - d.lightColor_.l);
                        return d.lightColor_;
                    })
                    .style('stroke', d => d3.rgb(d.lightColor_).darker(1));

                thisNode.append('rect')//smaller rect
                    .attr('height', d => d.trueDy)
                    .attr('width', sankey.nodeWidth())
                    .style('fill', d => d.color = color(d.name.replace(/ .*/, '')))
                    .style('stroke', d => d3.rgb(d.color).darker(3))
                    .append('title')
                    .text(function (d) {
                        return d.name + '\n' + d.trueValue + ' ' + linkUnit
                            + '\n(Double-click to filter for students who passed this node.)';
                    });
            }
        });


        nonZeroNodes.append('text')
            .attr('dy', '-0.5em')
            .attr('transform', 'rotate(-90)')
            .attr('text-anchor', 'end')
            .text(d => d.name)
            .classed("discussion", d => d.isDiscussion != undefined && d.isDiscussion == 1)
        ;



    };


    console.log("mode " +mode);


}