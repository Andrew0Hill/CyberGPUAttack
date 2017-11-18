
// Array to hold the number of collisions for each cell in the chart
var cells = [];

// Grid Size (in cells)
// will create a grid of num_cells * num_cells
var num_cells = 16;

// Size of a cell in pixels.
var cell_size_px = 35;

// Color scheme for collisions varies from white to red.
var color_scheme = d3.schemeReds[50];
var grid_frame = d3.select("body").append("svg").attr("width",700).attr("height",600);
// Sets up the cells of the grid.
function initialize(){
    for(var i = 0; i < num_cells; ++i){
        for(var j = 0; j < num_cells; ++j){
            var cell = {
                xpos : j*cell_size_px,
                ypos : i*cell_size_px,
                collisions : Math.random() * 50
            };
            cells.push(cell);
        }
    }
    drawGrid();
    d3.interval(updateGrid,2000);
}
function drawGrid(){
    for(var i = 0; i < cells.length; ++i){
        grid_frame
            .append("rect")
            .attr("width",cell_size_px)
            .attr("height",cell_size_px)
            .attr("x",cells[i].xpos)
            .attr("y",cells[i].ypos)
            .style("fill","#fff")
            .style("stroke","#000")
    }
}
// Updates the entire grid at once.
function updateGrid(){
    generateRandColls();
    var grid_cells = d3.selectAll("rect")
        .data(cells)
        .transition()
        .duration(1500)
        .style("fill",
            function(d){
                return d3.interpolateYlOrRd(d3.scaleLinear().domain([0,50]).range([0,1])(d.collisions));
            }
        )
}

function generateRandColls(){
    for(var i = 0; i < cells.length; ++i){
        cells[i].collisions = Math.random() * 50;
    }
}