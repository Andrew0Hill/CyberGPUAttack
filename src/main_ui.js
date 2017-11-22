
// Array to hold the number of collisions for each cell in the chart
var cells = [];

var width = document.getElementById('grid_div').clientWidth;
var height = document.documentElement.clientHeight;

// Grid Size (in cells)
// will create a grid of num_cells * num_cells
var num_cells = 16;

// Size of a cell in pixels.
var cell_size_px = 38;

// Variable to hold mouse position.
var mouse_pos = [];

function color_scale(color){
    return d3.interpolateYlOrRd(d3.scaleLinear().domain([0,20]).range([0,1])(color));
}
var vert_margin = Math.floor((width - (num_cells*cell_size_px))/2);
var horiz_margin = Math.floor((height - (num_cells*cell_size_px))/2);
var grid_frame = d3.select("#grid_div").append("svg").attr("width","100%").attr("height","100%").style("display","block");


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
                collisions : 0,
                index : (num_cells * j) + i
            };
            cells.push(cell);
            //document.writeln("x: " + cell.xpos + "y: " + cell.ypos);
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
            .on("mouseenter",mouseOn)
            .on("mouseleave",mouseOff)
            .style("fill","#fff")
            .style("stroke","#000")
    }
    //document.write("" + height + "  " + width);
}
function mouseOn(d,i){
    var rect_x;
    var rect_y;

    var text_x;
    // Draw the rectangle.
    if(d.xpos + 150 + cell_size_px + 8 > d3.select("#grid_div").node().getBoundingClientRect().width){
        rect_x = d.xpos - (8 + 150);
        text_x = rect_x + 7;
    }else{
        rect_x = d.xpos + cell_size_px + 8;
        text_x = rect_x + 7;
    }
    rect_y = d.ypos + 8;
    grid_frame
        .append("rect")
        .attr("x",rect_x)
        .attr("y",rect_y)
        .attr("rx",3)
        .attr("ry",3)
        .attr("width",150)
        .attr("height",30)
        .attr("class","hoverrect")
        .style("fill",color_scale(d.collisions))
        .style("stroke","#000");

    grid_frame.append("text")
        .text(function() {
            var ranges = getHexRange(d.index);
            return "Range: " + ranges[0] + "-" + ranges[1];
        })
        .attr("class","hovertext")
        .attr("font-family","sans-serif")
        .attr("font-size","10px")
        .attr("x",text_x)
        .attr("y",d.ypos + 22)
        .attr("fill","black")

    grid_frame.append("text")
        .text(function() {
            return d.collisions + " collisions."
        })
        .attr("class","hovertext")
        .attr("font-family","sans-serif")
        .attr("font-size","10px")
        .attr("x",text_x)
        .attr("y",d.ypos + 32)
        .attr("fill","black")

}
function mouseOff(d,i){
    d3.selectAll("text.hovertext").remove();
    d3.selectAll("rect.hoverrect").remove();
}

function getHexRange(index){
    var lower = index.toString(16).toUpperCase() + "000000";
    var upper = index.toString(16).toUpperCase() + "FFFFFF";
    return [lower,upper]
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
        .style("fill",function(d){
            return color_scale(d.collisions)
        })
}

function generateRandCols(){
    for(var i = 0; i < cells.length; ++i){
        cells[i].collisions = Math.random() * 50;
    }
}