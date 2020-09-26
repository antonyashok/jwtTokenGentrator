/*Derived from:
soxofaan:
demo - http://bl.ocks.org/soxofaan/7c96560677ead0425fe7
forum - https://github.com/d3/d3-plugins/issues/1#issuecomment-106638345
git - https://github.com/soxofaan/d3-plugin-captain-sankey
d3noob:
demo - https://bl.ocks.org/d3noob/013054e8d7807dff76247b81b0e29030*/
d3.sankey = function() {
  var sankey = {},
      nodeWidth = 24,
      nodePadding = 8,
      size = [1, 1],
      nodes = [],
      links = [],
      sinksRight = true,
      nodePitchX = 1;

  sankey.nodeWidth = function(_) {
    if (!arguments.length) return nodeWidth;
    nodeWidth = +_;
    return sankey;
  };

  sankey.nodePadding = function(_) {
    if (!arguments.length) return nodePadding;
    nodePadding = +_;
    return sankey;
  };

  sankey.nodes = function(_) {
    if (!arguments.length) return nodes;
    nodes = _;
    return sankey;
  };

  sankey.links = function(_) {
    if (!arguments.length) return links;
    links = _;
    return sankey;
  };

  sankey.size = function(_) {
    if (!arguments.length) return size;
    size = _;
    return sankey;
  };

  sankey.sinksRight = function (_) {
    if (!arguments.length) return sinksRight;
    sinksRight = _;
    return sankey;
  };

  sankey.nodePitchX = function () {
    return nodePitchX;
  }

  sankey.layout = function(minLinkSize) {
    computeNodeLinks();
    computeNodeValues();
    filterSmallLinks(minLinkSize);  //done AFTER computing node values to show true node sizes
    computeNodeBreadths();
    computeNodeDepths();
    return sankey;
  };

  sankey.relayout = function() {
    computeLinkDepths();
    return sankey;
  };

  // SVG path data generator, to be used as "d" attribute on "path" element selection.
  //returns array of 3 path strings for links to last node; or 1 path string otherwise
  sankey.link = function() {
    var curvature = 0.5;

    function link(d) {
      var xs = d.source.x + d.source.dx,
          xt = d.target.x,
          xi = d3.interpolateNumber(xs, xt),
          xsc = xi(curvature),
          xtc = xi(1 - curvature),
          ys = d.source.y + d.sy + d.dy / 2,
          yt = d.target.y + d.ty + d.dy / 2;

      //if target is last node
      if(d.target === nodes[nodes.length-1]){
        var start = `M ${xs},${ys} q ${nodePitchX/2},${0} ${nodePitchX},-100`;
        var full = `M ${xs},${ys} q ${nodePitchX/2},${0} ${nodePitchX},-100 `+
            `C ${xs+nodePitchX*1.5},${ys-200} ${xt-nodePitchX},${yt} ${xt-nodePitchX/2},${yt} `+
            `H ${xt}`;
        //nudge 1px; see https://stackoverflow.com/questions/13223636/svg-gradient-for-perfectly-horizontal-path
        var end = `M ${xt-nodePitchX/2},${yt+1} L ${xt},${yt}`;
        return [start, full, end];
      }
      //else, if target node is to the right of source node
      else if (xs < xt) {  //if (!d.cycleBreaker) {
        return "M" + xs + "," + ys
             + "C" + xsc + "," + ys
             + " " + xtc + "," + yt
             + " " + xt + "," + yt;
      }
      //else, target node is to the left of source node
      else {
        var xdelta = (1.5 * d.dy + 0.05 * Math.abs(xs - xt));
        xsc = xs + xdelta;
        xtc = xt - xdelta;
        var xm = xi(0.5);
        var ym = d3.interpolateNumber(ys, yt)(0.5);
        var ydelta = (2 * d.dy + 0.1 * Math.abs(xs - xt) + 0.1 * Math.abs(ys - yt)) * (ym < (size[1] / 2) ? -1 : 1);
        return "M" + xs + "," + ys
             + "C" + xsc + "," + ys
             + " " + xsc + "," + (ys + ydelta)
             + " " + xm + "," + (ym + ydelta)
             + "S" + xtc + "," + yt
             + " " + xt + "," + yt;
      }
    }

    link.curvature = function(_) {
      if (!arguments.length) return curvature;
      curvature = +_;
      return link;
    };

    return link;
  };

  // Populate the sourceLinks and targetLinks for each node.
  // Also, if the source and target are not objects, assume they are indices.
  function computeNodeLinks() {
    nodes.forEach(function(node) {
      // Links that have this node as source.
      node.sourceLinks = [];
      // Links that have this node as target.
      node.targetLinks = [];
    });
    links.forEach(function(link) {
      var source = link.source,
          target = link.target;
      if (typeof source === "number") source = link.source = nodes[link.source];
      if (typeof target === "number") target = link.target = nodes[link.target];
      source.sourceLinks.push(link);
      target.targetLinks.push(link);
    });
  }

  //compute the value (size) of each node by summing its links.
  function computeNodeValues() {
    nodes.forEach(function(node) {
      node.value = Math.max(
        d3.sum(node.sourceLinks, value),
        d3.sum(node.targetLinks, value)
      );
    });
  }

  //Filter out links smaller than a threshold size
  function filterSmallLinks(minLinkSize) {
    if(!arguments.length) //if no minimum link size is provided, do nothing
      return;

    var notSmallLinks=[];
    links.forEach(function(link){
      if(link.value >= minLinkSize)
        notSmallLinks.push(link);
    });
    links=notSmallLinks;  //re-point 'links' to the filtered array

    nodes.forEach(function(node){
      var notSmallLinks=[];
      node.sourceLinks.forEach(function(link){
        if(link.value >= minLinkSize)
          notSmallLinks.push(link);
      });
      node.sourceLinks=notSmallLinks;

      notSmallLinks=[];
      node.targetLinks.forEach(function(link){
        if(link.value >= minLinkSize)
          notSmallLinks.push(link);
      });
      node.targetLinks=notSmallLinks;
    });
  }

  //Assign the breadth (x-position) of each node in a grid-like pattern
  function computeNodeBreadths() {
    var x = 0;
    var nextNodeIsRepeat = false;
    nodes.forEach(function(node){
      node.x = x;
      node.dx = nodeWidth;

      if(!nextNodeIsRepeat)
        x++;

      nextNodeIsRepeat=!nextNodeIsRepeat;
    });

    nodePitchX = (size[0] - nodeWidth) / x;
    scaleNodeBreadths(nodePitchX);
  }

/*
  // Iteratively assign the breadth (x-position) for each node.
  // Nodes are assigned the maximum breadth of incoming neighbors plus one;
  // nodes with no incoming links are assigned breadth zero, while
  // nodes with no outgoing links are assigned the maximum breadth.
  function computeNodeBreadths() {
    var remainingNodes = nodes,
        nextNodes,
        x = 0;

    // Work from left to right.
    // Keep updating the breath (x-position) of nodes that are target of recently updated nodes.
    while (remainingNodes.length && x < nodes.length) {
      nextNodes = [];
      remainingNodes.forEach(function(node) {
        node.x = x;
        node.dx = nodeWidth;
        node.sourceLinks.forEach(function(link) {
          if (nextNodes.indexOf(link.target) < 0 && !link.cycleBreaker) {
            nextNodes.push(link.target);
          }
        });
      });
      if (nextNodes.length == remainingNodes.length) {
        // There must be a cycle here. Let's search for a link that breaks it.
        findAndMarkCycleBreaker(nextNodes);
        // Start over.
        // TODO: make this optional?
        return computeNodeBreadths();
      }
      else {
        remainingNodes = nextNodes;
        ++x;
      }
    }

    // Optionally move pure sinks always to the right.
    if (sinksRight) {
      moveSinksRight(x);
    }
  }
*/

  // Find a link that breaks a cycle in the graph (if any).
  function findAndMarkCycleBreaker(nodes) {
  // Go through all nodes from the given subset and traverse links searching for cycles.
    var link;
    for (var n=nodes.length - 1; n >= 0; n--) {
      link = depthFirstCycleSearch(nodes[n], []);
      if (link) {
        return link;
      }
    }

    // Depth-first search to find a link that is part of a cycle.
    function depthFirstCycleSearch(cursorNode, path) {
      var target, link;
      for (var n = cursorNode.sourceLinks.length - 1; n >= 0; n--) {
        link = cursorNode.sourceLinks[n];
        if (link.cycleBreaker) {
          // Skip already known cycle breakers.
          continue;
        }

        // Check if target of link makes a cycle in current path.
        target = link.target;
        for (var l = 0; l < path.length; l++) {
          if (path[l].source == target) {
            // We found a cycle. Search for weakest link in cycle
            var weakest = link;
            for (; l < path.length; l++) {
              if (path[l].value < weakest.value) {
                weakest = path[l];
              }
            }
            // Mark weakest link as (known) cycle breaker and abort search.
            weakest.cycleBreaker = true;
            return weakest;
          }
        }

        // Recurse deeper.
        path.push(link);
        link = depthFirstCycleSearch(target, path);
        path.pop();
        // Stop further search if we found a cycle breaker.
        if (link) {
          return link;
        }
      }
    }
  }


  function moveSourcesRight() {
    nodes.forEach(function(node) {
      if (!node.targetLinks.length) {
        node.x = d3.min(node.sourceLinks, function(d) { return d.target.x; }) - 1;
      }
    });
  }

  function moveSinksRight(x) {
    nodes.forEach(function(node) {
      if (!node.sourceLinks.length) {
        node.x = x - 1;
      }
    });
  }

  function scaleNodeBreadths(kx) {
    nodes.forEach(function(node) {
      node.x *= kx;
    });
  }

  // Compute the depth (y-position) for each node.
  function computeNodeDepths() {
    // Group nodes by breadth.
    var nodesByBreadth = d3.nest()
        .key(function(d) { return d.x; })
        .entries(nodes)
        .map(function(d) { return d.values; });

    /*for (var alpha = 1; iterations > 0; --iterations) {
      relaxRightToLeft(alpha *= .99);
      resolveCollisions();
      computeLinkDepths();
      relaxLeftToRight(alpha);
      resolveCollisions();
      computeLinkDepths();
    }*/

    initializeNodeDepth();
    resolveCollisions();
    computeLinkDepths();

    function initializeNodeDepth() {
      //get unscaled height of end node
      var topNodeValue = nodes[nodes.length-1].value;


      // Calculate vertical scaling factor.
      var ky = d3.min(nodesByBreadth, function(nodes) {
        return (size[1] - nodePadding) / (d3.sum(nodes, value) + topNodeValue);
      });

      nodesByBreadth.forEach(function(nodes) {
        nodes.forEach(function(node, i) {
          node.y = i;
          node.dy = node.value * ky;  //add 1/3 factor to not use full height?

          //special case: if the node's data value is NOT just the sum of the link data values entering and exiting it,
          // a 'trueValue' property will be present; compute trueDy from it
          if(node.trueValue!==undefined)
            node.trueDy = node.trueValue * ky;  //special case
        });
      });

      //bump all nodes down by end node's scaled height, except end node
      const topNodeDy = topNodeValue*ky;
      for(let n=0; n<nodes.length-1; n++){
        nodes[n].y += topNodeDy/2;
      }


      links.forEach(function(link) {
        link.dy = link.value * ky;
      });
    }

    function relaxLeftToRight(alpha) {
      nodesByBreadth.forEach(function(nodes, breadth) {
        nodes.forEach(function(node) {
          if (node.targetLinks.length) {
            // Value-weighted average of the y-position of source node centers linked to this node.
            var y = d3.sum(node.targetLinks, weightedSource) / d3.sum(node.targetLinks, value);
            node.y += (y - center(node)) * alpha;
          }
        });
      });

      function weightedSource(link) {
        return (link.source.y + link.sy + link.dy / 2) * link.value;
      }
    }

    function relaxRightToLeft(alpha) {
      nodesByBreadth.slice().reverse().forEach(function(nodes) {
        nodes.forEach(function(node) {
          if (node.sourceLinks.length) {
            // Value-weighted average of the y-positions of target nodes linked to this node.
            var y = d3.sum(node.sourceLinks, weightedTarget) / d3.sum(node.sourceLinks, value);
            node.y += (y - center(node)) * alpha;
          }
        });
      });

      function weightedTarget(link) {
        return (link.target.y + link.ty + link.dy / 2) * link.value;
      }
    }

    function resolveCollisions() {
      nodesByBreadth.forEach(function(nodes) {
        var node,
            dy,
            y0 = 0,
            n = nodes.length,
            i;

        //Push any overlapping nodes down.
        nodes.sort(ascendingDepth);
        for (i = 0; i < n; ++i) {
          node = nodes[i];
          dy = y0 - node.y;
          if (dy > 0) node.y = 750;
          y0 = node.y + node.dy + nodePadding;

          if(node.isDiscussion != undefined){
            node.y = node.y - node.dy/2;
          }
        }

        // If the bottommost node goes outside the bounds, push it back up.
        dy = y0 - nodePadding - size[1];
        if (dy > 0) {
          y0 = node.y -= dy;

          // Push any overlapping nodes back up.
          for (i = n - 2; i >= 0; --i) {
            node = nodes[i];
            dy = node.y + node.dy + nodePadding - y0;
            if (dy > 0) node.y -= dy;
            y0 = node.y;
          }
        }
      });
    }

    function ascendingDepth(a, b) {
      return a.y - b.y;
    }
  }

  // Compute y-offset of the source endpoint (sy) and target endpoints (ty) of links,
  // relative to the source/target node's y-position.
  function computeLinkDepths() {
    nodes.forEach(function(node) {
      node.sourceLinks.sort(ascendingTargetDepth);
      node.targetLinks.sort(ascendingSourceDepth);
    });
    nodes.forEach(function(node) {
      var sy = 0, ty = 0;
      node.sourceLinks.forEach(function(link) {
        link.sy = sy;
        sy += link.dy;
      });
      node.targetLinks.forEach(function(link) {
        link.ty = ty;
        ty += link.dy;
      });
    });

    function ascendingSourceDepth(a, b) {
      return a.source.y - b.source.y;
    }

    function ascendingTargetDepth(a, b) {
      return a.target.y - b.target.y;
    }
  }

  // Y-position of the middle of a node.
  function center(node) {
    return node.y + node.dy / 2;
  }

  // Value property accessor.
  function value(x) {
    return x.value;
  }

  return sankey;
};
