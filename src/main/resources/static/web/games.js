function login(evt) {
  //evt.preventDefault();
  //var form = evt.target.form;
  $.post("/api/login", 
         { name: document.getElementById("login-username").value,
           pwd: document.getElementById("login-password").value })
   .done(function(){
      window.location.reload()
  })
   .fail(function(){
      alert("Invalid username or password. Try again!")
  });
}

function singup(evt) {
  $.post("/api/players", 
         { userName: document.getElementById("signup-username").value,
           password: document.getElementById("signup-password").value })
   .done(function(){
      $.post("/api/login", 
         { name: document.getElementById("signup-username").value,
           pwd: document.getElementById("signup-password").value })
      .done(function(){
          window.location.reload()
      })
   })
   .fail(function(){
      alert("Invalid username or password. Try again!")
  });
}

function logout(evt) {
  evt.preventDefault();
  $.post("/api/logout")
   .done(function(){
      window.location.reload()
  })
   .fail();
}


function createGame (){
    $.post("/api/games")
    .done(function(response){
        window.location.href = "/web/game.html?gp="+response.gpid
    })
    .fail(function(){
        alert("Error creating game.")
    })
}

function joinGame (gameId){
    $.post("/api/game/" +gameId+ "/players")
    .done(function(gamesData){
        window.location.href = "/web/game.html?gp="+gamesData.gpid
    })
    .fail(function(){
        alert("Error joining game.")
    })
}


let gamesData

const loadData = () => {
	fetch('/api/games')
	.then(response => response.json())
	.then(json => {
		gamesData = json.games
        app.currentPlayer = json.player
		changeDateFormat()
		gamesTable()
		leaderTable(leaderboard(), document.getElementById('leaderboard'))
	})
	.catch(error => console.log(error))
}

loadData()

var app = new Vue({
    el: "#app",
    data: {
        gameData: [],
        currentPlayer: []
    }
});

const changeDateFormat = () => {
    for (var i in gamesData){
        var newDate = new Date(gamesData[i].created).toLocaleString();
        gamesData[i].created = newDate
    }
}



const gamesTable = () => {
	let table = document.getElementById('games-table')
	let head = document.createElement('THEAD')
	let row = document.createElement('TR')
	let cell1 = document.createElement('TH')
	cell1.innerText = '#'
	row.appendChild(cell1)
	let cell2 = document.createElement('TH')
	cell2.innerText = 'created'
	row.appendChild(cell2)
	let cell3 = document.createElement('TH')
	cell3.innerText = 'players'
	cell3.colSpan = 2
	row.appendChild(cell3)
	head.appendChild(row)

	table.appendChild(head)


	let body = document.createElement('TBODY')
	gamesData.forEach(game => {
		let row = document.createElement('TR')
		for(item in game){
			
			if(typeof game[item] == 'object'){
				if(game[item].length == 1){
					let cell = document.createElement('TD')
					let cell2 = document.createElement('TD')
					cell.innerText = game[item][0].player.username
					cell2.innerText = 'waiting...'
					row.appendChild(cell)
					row.appendChild(cell2)
				} else {
					game[item].forEach(gamePlayer => {
						let cell = document.createElement('TD')
						cell.innerText = gamePlayer.player.username
						row.appendChild(cell)
					})
				}
				
			} else{
				let cell = document.createElement('TD')
				cell.innerText = game[item]
				row.appendChild(cell)
			}
			
		}

		body.appendChild(row)

	})

	table.appendChild(body)
}

const leaderboard = () => {
	let leaderboard = []
	let aux = []

	gamesData.forEach(game => {
		game.gamePlayers.forEach(gamePlayer => {
			if(aux.indexOf(gamePlayer.player.id) == -1){
				aux.push(gamePlayer.player.id)
				let obj = {}
				obj.id = gamePlayer.player.id
				obj.email = gamePlayer.player.username
				obj.score = gamePlayer.score
				obj.won = gamePlayer.score == 3 ? 1 : 0
				obj.lost = gamePlayer.score == 0 ? 1 : 0
				obj.tied = gamePlayer.score == 1 ? 1 : 0
				leaderboard.push(obj)
			} else{
				leaderboard.forEach(player => {
					if(player.id == gamePlayer.player.id){
						player.score += gamePlayer.score
						player.won += gamePlayer.score == 3 ? 1 : 0
						player.lost += gamePlayer.score == 0 ? 1 : 0
						player.tied += gamePlayer.score == 1 ? 1 : 0
					}
				})
			}
		})
	})
	return leaderboard
}

const leaderTable = (leaders, table) => {
	let head = document.createElement('THEAD')
	let row = document.createElement('TR')
	for (key in leaders[0]){
		let cell = document.createElement('TH')
		cell.innerText = key
		row.appendChild(cell)
	}
	head.appendChild(row)
	
    let body = document.createElement('TBODY')
    leaders.forEach(leader => {
		let row = document.createElement('TR')
		for(item in leader){
			let cell = document.createElement('TD')
			cell.innerText = leader[item]
			row.appendChild(cell)
		}
		body.appendChild(row)
	})

	table.appendChild(head)
	table.appendChild(body)
}