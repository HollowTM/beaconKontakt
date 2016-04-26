var myCanvas;
var myCanvas2dContext;
var myStation;

var myStationImg;
var myBeaconImg;

var getterURL = "getter";
var canvasID = "myCanvas";
var stationImgId = "scream";
var beacionImgId = "beacon";
var imgWidth = 50;
var imgHeight = 50;
var canvasWidth = 400;
var canvasHeight = 600;
var xMax = 4;
var yMax = 6;

var iFrequency = 1050; // expressed in miliseconds
var iFrequency2 = 1000; // expressed in miliseconds
var iFrequency3 = 5000; // expressed in miliseconds
var myInterval = 0;
var myInterval2 = 0;
var myInterval3 = 0;

var scanStation0 = false;
var scanStation1 = false;
var scanStation2 = false;
var scanStation3 = false;

var myBeacons = "{}";

function start() {
	myCanvas = $("#" + canvasID);
	myCanvas2dContext = myCanvas[0].getContext("2d");
	// Steuert man width/height über css an, wird neu scaliert und das Bild wird
	// verzerrt
	myCanvas.attr("width", canvasWidth);
	myCanvas.attr("height", canvasHeight);

	myStationImg = document.getElementById(stationImgId);
	myBeaconImg = document.getElementById(beacionImgId);

	myCanvas.click(function(evt) {
		var mousePos = getMousePos(c, evt);
		if (mousePos.x < 50 && mousePos.y < 50) {
			scanStation0 = !scanStation0;
		} else if (mousePos.x > 750 && mousePos.y < 50) {
			scanStation1 = !scanStation1;

		} else if (mousePos.x < 50 && mousePos.y > 550) {
			scanStation2 = !scanStation2;

		} else if (mousePos.x > 750 && mousePos.y > 550) {
			scanStation3 = !scanStation3;
		}
	}, false);

	getBeaconAndPositions();

	startLoop();
	startLoop2();
	startLoopGetBeacons();
}

// STARTS and Resets the loop if any
function startLoop() {
	if (myInterval > 0)
		clearInterval(myInterval); // stop
	myInterval = setInterval("reset()", iFrequency); // run
}
// STARTS and Resets the loop if any
function startLoop2() {
	if (myInterval2 > 0)
		clearInterval(myInterval2); // stop
//	myInterval2 = setInterval("scan(340,360)", iFrequency2); // run
}

// STARTS and Resets the loop if any
function startLoopGetBeacons() {
	if (myInterval3 > 0)
		clearInterval(myInterval3); // stop
	myInterval3 = setInterval("getBeaconAndPositions()", iFrequency3); // run
}

function getBeaconAndPositions() {
	$.ajax({
		url : getterURL,
		success : function(result) {
			if (result != "error")
				myBeacons = result;
		}
	});
}

function reset() {
	myCanvas2dContext.beginPath();
	myCanvas2dContext.rect(0, 0, canvasWidth, canvasHeight);
	myCanvas2dContext.fillStyle = "lightgrey";
	myCanvas2dContext.fill();

	// for each station : draw station
	for (i = 0; i < ((myBeacons.stations) ? myBeacons.stations.length : 0); i++) {
		drawStation(myBeacons.stations[i]);
	}
	// for each station : draw station
	for (i = 0; i < ((myBeacons.beacons) ? myBeacons.beacons.length : 0); i++) {
		drawBeacon(myBeacons.beacons[i]);
	}
}
function drawStation(device) {
	newY = device.yPos * canvasHeight / yMax - imgHeight / 2;
	newX = device.xPos * canvasWidth / xMax - imgWidth / 2;

	// Randbetrachtung
	newX = (newX < imgWidth / 2) ? imgWidth / 2 : newX;
	newY = (newY < imgHeight / 2) ? imgHeight / 2 : newY;

	myCanvas2dContext.drawImage(myStationImg, newX, newY, imgWidth, imgHeight);

	// console.log();
	// console.log(myBeacons.beacons[i].clientID);
	// console.log("Y=", myBeacons.beacons[i].position[0].yPos, ":", newY);
	// console.log("X=", myBeacons.beacons[i].position[0].xPos, ":", newX);
}

function drawBeacon(device) {
	newY = device.position[0].yPos * canvasHeight / yMax - imgHeight / 2;
	newX = device.position[0].xPos * canvasWidth / xMax - imgWidth / 2;

	// Randbetrachtung
	newX = (newX < imgWidth / 2) ? imgWidth / 2 : newX;
	newY = (newY < imgHeight / 2) ? imgHeight / 2 : newY;

	myCanvas2dContext.drawImage(myBeaconImg, newX, newY, imgWidth, imgHeight);
}

function getMousePos(canvas, evt) {
	var rect = canvas.getBoundingClientRect();
	return {
		x : evt.clientX - rect.left,
		y : evt.clientY - rect.top
	};
}

function showRange(device, color) {

	if (scanStation0) {
		myCanvas2dContext.beginPath();
		myCanvas2dContext.strokeStyle = color;
		myCanvas2dContext.arc(0, 0, Math.sqrt(Math.pow(x, 2) + Math.pow(y, 2)),
				0, 2 * Math.PI);
		myCanvas2dContext.stroke();
	}

	if (scanStation1) {
		myCanvas2dContext.beginPath();
		myCanvas2dContext.strokeStyle = "blue";
		myCanvas2dContext.arc(width, 0, Math.sqrt(Math.pow(canvasWidth - x, 2)
				+ Math.pow(y, 2)), 0, 2 * Math.PI);
		myCanvas2dContext.stroke();
	}

	if (scanStation2) {
		myCanvas2dContext.beginPath();
		myCanvas2dContext.strokeStyle = "yellow";
		myCanvas2dContext.arc(device.xPos, device.yPos, Math.sqrt(Math.pow(x, 2)
				+ Math.pow(canvasHeight - y, 2)), 0, 2 * Math.PI);
		myCanvas2dContext.stroke();
	}
	if (scanStation3) {
		myCanvas2dContext.beginPath();
		myCanvas2dContext.strokeStyle = "green";
		myCanvas2dContext.arc(width, height, Math.sqrt(Math.pow(
				canvasWidth - x, 2)
				+ Math.pow(canvasHeight - y, 2)), 0, 2 * Math.PI);
		myCanvas2dContext.stroke();
	}

}