
// Array to hold the number of collisions for each cell in the chart
var cells = [];

var width = document.getElementById('grid_div').clientWidth;
var height = document.documentElement.clientHeight;

// Grid Size (in cells)
// will create a grid of num_cells * num_cells
var num_cells = 16;

// Size of a cell in pixels.
var cell_size_px = 35;

var vert_margin = Math.floor((width - (num_cells*cell_size_px))/2);
var horiz_margin = Math.floor((height - (num_cells*cell_size_px))/2);
var grid_frame = d3.select("#grid_div").append("svg").attr("width",width).attr("height","100vh").style("display","block");


/*
// Function to set up the cells of a grid. For each element in the cells array, we create a
// 'collisions' member, as well as two members which control the position of this cell in the grid.
// The 'collision' member will be accessed and updated through the JavaFX code.
*/
function initialize(){
    for(var i = 0; i < num_cells; ++i){
        for(var j = 0; j < num_cells; ++j){
            var cell = {
                xpos : (j*cell_size_px)+vert_margin,
                ypos : (i*cell_size_px)+horiz_margin,
                collisions : 0
            };
            cells.push(cell);
        }
    }
    drawGrid();
    d3.interval(updateGrid,2000);
}

/*
// Function to draw the grid based on the cell data inside the cell[] array.
// This is called once to set up the grid. Once the grid is drawn, we can update
// the data for each cell using the setCells() method, and then call updateGrid()
// to show the new data inside the grid.
*/
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
    //document.write("" + height + "  " + width);
}

// Accepts an array of cell data, and assigns each cell[] member the data that is passed to it.
function setCells(arr){
    for(var i = 0; i < cells.length; ++i){
        cells[i].collisions = arr[i];
    }
}

// Updates the entire grid at once.
function updateGrid(){
    var grid_cells = d3.selectAll("rect")
        .data(cells)
        .transition()
        .duration(1500)
        .style("fill",
            function(d){
                return d3.interpolateReds(d3.scaleLinear().domain([0,4]).range([0,1])(d.collisions));
            }
        )
}

function generateRandCols(){
    for(var i = 0; i < cells.length; ++i){
        cells[i].collisions = Math.random() * 50;
    }
}