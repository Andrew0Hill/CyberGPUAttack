var w = 410;
var h = 300;
var x = d3.scale.linear().domain([0, 70]).range([0,w]);
var y = d3.scale.linear().domain([0, 1]).range([h,0]);

var pad = 50;
var svg = d3.select("body")
    .append("svg:svg")
    .attr("height", h + pad)
    .attr("width",  w + pad)

var vis = svg.append("svg:g")
    .attr("transform", "translate(40,20)")

var legend = d3.select("body").append("div")
    .classed("legend", true)

var continuous = make_birthday_function();

make_rules();
chart_line();
make_mouseover_guides();


function make_birthday_function(total) {
  var e = Math.E, pow = Math.pow;
  return (function(xi) {
      return 1 - (pow(e,-(pow(xi,2)/(2*365))))
    });
}

function chart_line() {
  var g = vis.append("svg:g")
      .classed("series", true)

  g.append("svg:path")
      .attr("d", function(d) { return d3.svg.line()(
        x.ticks(100).map(function(xi) {
          return [ x(xi), y(continuous(xi)) ]
        })
       )})
}

function make_mouseover_guides() {
  var guides = vis.append("svg:g")
          .classed("guides", true)
  var y_guides = guides.append("svg:g")
  guides.append("svg:line")
          .attr("y1",h)
  y_guides.append("svg:circle")
          .attr("r",7)
  y_guides.append("svg:line")
          .attr("x1",-20)
          .attr("x2",+20)

  vis.append("svg:rect")
      .classed("mouse_area", true)
      .attr("width",  w)
      .attr("height", h)
      .on("mousemove", update_legend_values)
      .on("mouseout",   blank_legend_values)

  blank_legend_values();

  var format_5f = d3.format(".5f");

  function update_legend_values() {
    var xi = x.invert(d3.svg.mouse(this)[0]);

    legend
        .text("Number of People: "+ d3.format(".2f")(xi) + "     |     Probability of a Match (Same Birthday): "+format_5f(continuous(xi) * 100)+"%");

    guides
        .attr("transform", "translate("+(x(xi))+",0)")
        .attr("visibility", "visible")

    y_guides
        .attr("transform", "translate(0,"+y(continuous(xi))+")")
  }

  function blank_legend_values() {
    legend
        .text("Birthday Problem Probability")

    guides
        .attr("visibility", "hidden")
  }
}


function make_rules() {
  var rules = vis.append("svg:g").classed("rules", true)

  function make_x_axis() {
    return d3.svg.axis()
        .scale(x)
        .orient("bottom")
        .ticks(10)
  }

  function make_y_axis() {
    return d3.svg.axis()
        .scale(y)
        .orient("left")
        .ticks(10)
  }

  rules.append("svg:g").classed("grid x_grid", true)
      .attr("transform", "translate(0,"+h+")")
      .call(make_x_axis()
        .tickSize(-h,0,0)
        .tickFormat("")
      )

  rules.append("svg:g").classed("grid y_grid", true)
      .call(make_y_axis()
        .tickSize(-w,0,0)
        .tickFormat("")
      )

  rules.append("svg:g").classed("labels x_labels", true)
      .attr("transform", "translate(0,"+h+")")
      .call(make_x_axis()
        .tickSize(5)
      )

  rules.append("svg:g").classed("labels y_labels", true)
      .call(make_y_axis()
        .tickSubdivide(1)
        .tickSize(10, 5, 0)
      )
}

