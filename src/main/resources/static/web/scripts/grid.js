
//main function that shoots the gridstack.js framework and loads the grid with the ships
const loadGrid = function (isStatic) {
    var options = {
        //10 x 10 grid
        width: 10,
        height: 10,
        //space between elements (widgets)
        verticalMargin: 0,
        //height of cells
        cellHeight: 45,
        //disables resizing of widgets
        disableResize: true,
        //floating widgets
		float: true,
        //removeTimeout: 100,
        //allows the widget to occupy more than one column
        disableOneColumnMode: true,
        //false allows widget dragging, true denies it
        staticGrid: isStatic,
        //activates animations
        animate: true
    }
    //grid initialization
    $('.grid-stack').gridstack(options);

    grid = $('#grid').data('gridstack');


    createGrid(11, $(".grid-ships"), 'ships')


    if(!isStatic){
        grid.addWidget($('<div id="carrier"><div class="grid-stack-item-content carrierHorizontal"></div><div/>'),
            1, 0, 5, 1);
        grid.addWidget($('<div id="battleship"><div class="grid-stack-item-content battleshipHorizontal"></div><div/>'),
            2, 1, 4, 1);
        grid.addWidget($('<div id="submarine"><div class="grid-stack-item-content submarineHorizontal"></div><div/>'),
            3, 2, 3, 1);
        grid.addWidget($('<div id="destroyer"><div class="grid-stack-item-content destroyerHorizontal"></div><div/>'),
            4, 3, 3, 1);
        grid.addWidget($('<div id="patrol_boat"><div class="grid-stack-item-content patrol_boatHorizontal"></div><div/>'),
            5, 4, 2, 1);
        rotateShips("carrier", 5)
        rotateShips("battleship", 4)
        rotateShips("submarine",3)
        rotateShips("destroyer", 3)
        rotateShips("patrol_boat",2)
    } else {
        setShips()
    }

    listenBusyCells('ships')
    $('.grid-stack').on('change', function(){
       listenBusyCells('ships') 
    })


    //all the functionalities are explained in the gridstack github
    //https://github.com/gridstack/gridstack.js/tree/develop/doc
    
}


//creates the grid structure
const createGrid = function(size, element, id){

    let wrapper = document.createElement('DIV')
    wrapper.classList.add('grid-wrapper')

    for(let i = 0; i < size; i++){
        let row = document.createElement('DIV')
        row.classList.add('grid-row')
        row.id =`${id}grid-row${i}`
        wrapper.appendChild(row)

        for(let j = 0; j < size; j++){
            let cell = document.createElement('DIV')
            cell.classList.add('grid-cell')
            if(i > 0 && j > 0)
            cell.id = `${id}${i - 1}${ j - 1}`

            if(j===0 && i > 0){
                let textNode = document.createElement('SPAN')
                textNode.innerText = String.fromCharCode(i+64)
                cell.appendChild(textNode)
            }
            if(i === 0 && j > 0){
                let textNode = document.createElement('SPAN')
                textNode.innerText = j
                cell.appendChild(textNode)
            }
            row.appendChild(cell)
        }
    }

    element.append(wrapper)
}

//adds a listener to the ships, wich shoots its rotation when clicked
const rotateShips = function(shipType, cells){

        $(`#${shipType}`).click(function(){
        	document.getElementById("alert-text").innerHTML = ""
            let x = +($(this).attr('data-gs-x'))
            let y = +($(this).attr('data-gs-y'))
        if($(this).children().hasClass(`${shipType}Horizontal`)){
        	if(grid.isAreaEmpty(x,y+1,1,cells) || y + cells < 10){
	            if(y + cells - 1 < 10){
	                grid.resize($(this),1,cells);
	                $(this).children().removeClass(`${shipType}Horizontal`);
	                $(this).children().addClass(`${shipType}Vertical`);
	            } else{
	            	
	            		grid.update($(this), null, 10 - cells)
	                	grid.resize($(this),1,cells);
	                	$(this).children().removeClass(`${shipType}Horizontal`);
	                	$(this).children().addClass(`${shipType}Vertical`);
	            }
                
                
            }else{
            		document.getElementById("alert-text").innerHTML = "A ship is blocking the way!"
            }
            
        }else{
            if(x + cells - 1  < 10){
                grid.resize($(this),cells,1);
                $(this).children().addClass(`${shipType}Horizontal`);
                $(this).children().removeClass(`${shipType}Vertical`);
            } else{
                grid.update($(this), 10 - cells)
                grid.resize($(this),cells,1);
                $(this).children().addClass(`${shipType}Horizontal`);
                $(this).children().removeClass(`${shipType}Vertical`);
            }
            
        }
    });

}

//loops over all the grid cells, verifying if they are empty or busy
const listenBusyCells = function(id){
    for(let i = 0; i < 10; i++){
        for(let j = 0; j < 10; j++){
            if(!grid.isAreaEmpty(i,j)){
                $(`#${id}${j}${i}`).addClass('busy-cell').removeClass('empty-cell')
            } else{
                $(`#${id}${j}${i}`).removeClass('busy-cell').addClass('empty-cell')
            }
        }
    }
}


//gets the locations of the ships from the back-end
const setShips = function(){

    for(i = 0; i < gamesData.ships.length; i++){
        //only the first position of a ship is needed. The remaining positions are given by the orientation and the number of cells
        let shipType = (gamesData.ships[i].type).toLowerCase()
        let x = +(gamesData.ships[i].locations[0].slice(1)) - 1 //the number of the first position belongs to the x axis. To match the framework structure beginning at 0, we must substract 1 from it
        let y = stringToInt(gamesData.ships[i].locations[0][0].toUpperCase())//the letter of the first position belongs to y axis. In this case we must transform the string to a number, starting from 0.
        let w
        let h
        let orientation

        if(gamesData.ships[i].locations[0][0] == gamesData.ships[i].locations[1][0]){
            //if the letter of the first position is equal to letter of the second position, the ship orientation is horizontal. 
            //Therefore, the width is equal to the length of the location array and the height is equal to 1
            w = gamesData.ships[i].locations.length
            h = 1
            orientation = "Horizontal"
        } else{
            h = gamesData.ships[i].locations.length
            w = 1
            orientation = "Vertical"
        }

        //Finally, the addWidget function adds the ships to the grid
        grid.addWidget($('<div id="'+shipType+'"><div class="grid-stack-item-content '+shipType+orientation+'"></div><div/>'),
        x, y, w, h);



    }
}

function sendShips() {
   let shipsData = []
   var info = document.querySelectorAll(".grid-stack-item")
   var ships = Array.from(info)
   ships.forEach(ship => {
       let shipData = {};
       let shipLoc = [];
       let height = ship.dataset.gsHeight;
       let width = ship.dataset.gsWidth;
       let x = parseInt(ship.dataset.gsX);
       let y = parseInt(ship.dataset.gsY);
       if (width > height) {
           for (let i = 0; i < width; i++) {
               shipLoc.push(getLocation(y) + (x + i + 1))
           }
       } else {
           for (let i = 0; i < height; i++) {
               shipLoc.push(getLocation(y + i) + (x + 1))
           }
       }
   })
   console.log(ships)
}

//gets the locations of the salvoes from the back-end
const setSalvoes = function(){
    for(i = 0; i < gamesData.salvoes.length; i++){
        for(j = 0; j < gamesData.salvoes[i].locations.length; j++){
            let turn = gamesData.salvoes[i].turn
            let player = gamesData.salvoes[i].player
            let x = +(gamesData.salvoes[i].locations[j][1]) - 1
            let y = stringToInt(gamesData.salvoes[i].locations[j][0].toUpperCase())
            let isHit = gamesData.salvoes[i].hits.indexOf(gamesData.salvoes[i].locations[j]) != - 1 ? true : false

            if(player == actualPlayer.id){
                document.getElementById(`salvoes${y}${x}`).classList.add('salvo')
                document.getElementById(`salvoes${y}${x}`).innerHTML = `<span>${turn}</span>`
                if(isHit){
                    document.getElementById(`salvoes${y}${x}`).classList.add('hit')
                }
            }else{
                if(document.getElementById(`ships${y}${x}`).className.indexOf('busy-cell') != -1){
                    document.getElementById(`ships${y}${x}`).classList.remove('busy-cell')
                    document.getElementById(`ships${y}${x}`).classList.add('ship-down')
                    document.getElementById(`ships${y}${x}`).innerHTML = `<span>${turn}</span>`
                }
            }

        }
    }
}

const stringToInt = function(str){
    switch(str){
        case "A": 
            return 0;
        case "B": 
            return 1;
        case "C": 
            return 2;
        case "D": 
            return 3;
        case "E": 
            return 4;
        case "F": 
            return 5;
        case "G": 
            return 6;
        case "H": 
            return 7;
        case "I": 
            return 8;
        case "J": 
            return 9;
    }
}