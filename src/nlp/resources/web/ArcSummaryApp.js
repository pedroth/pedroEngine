var numberOfClusters;
var isLoading;
var isFinished = false;
var timeOutTime = 100;
var uID = Math.random() * new Date().getTime();
var videoAddress = [];
var videoAddressReady = [];

function waitTimeInSeconds(time) {
    var oldTime = new Date().getTime();
    var t = 0;
    do {
        var dt = (new Date().getTime() - oldTime) * 1E-3;
        oldTime = new Date().getTime();
        t += dt;
    } while(time - t > 0);
}

function createVideoTag(uID, clusterId, videoAddressUrl, videoId) {
    var address = "summary" + uID + "/" + clusterId + "/" + videoAddressUrl;
    var videoDiv = document.getElementById("video" + clusterId);
    while( videoDiv.childNodes.length > 0) {
        videoDiv.removeChild(videoDiv.childNodes[0]);
    }
    $("#video" + clusterId).append("<video width='320' height='240' controls> <source src='" + address + "' type='video/mp4'> Your browser does not support the video tag.</video>");
    $("#video" + clusterId).append("<a href='" + address + "'> download video</a>");
}

function appendVideos(videos, clusterId) {
    //create div
    var div = document.createElement('div');
    div.id = "cluster" + clusterId;
    div.class = "form-group row";
    //create label
    var label = document.createElement('label');
    label.class = "col-sm-2 control-label";
    label.innerHTML = "cluster " + clusterId;
    $("body").append(div);
    $("#" + div.id).append(label);
    $("#" + div.id).append("<br><br>");

    //append videos to div
    for (var j = 0; j < videos.length; j++) {
        var videoAddressUrl = encodeURIComponent(videos[j]);
        var videoButton = "<button class ='btn btn-primary' onclick=\"createVideoTag("  + uID +  "," + clusterId + ",'" + videoAddressUrl + "','video" + clusterId + "')\" >" + videos[j] + "</button>";
        $("#" + div.id).append(videoButton);
    }
    $("#" + div.id).append("<div id='video" + clusterId + "'></div>");
}

function buildVideos() {
    for(var i = 0; i < videoAddress.length; i++) {
        appendVideos(videoAddress[i], i);
    }
}

function checkIfVideosAreReady() {
    var andIdentity = true;
    for ( var i = 0; i < videoAddressReady.length; i++ ) {
        andIdentity = videoAddressReady[i] && andIdentity;
    }
    if(andIdentity) {
        buildVideos();
    }else {
        setTimeout(checkIfVideosAreReady, timeOutTime);
    }
}

function getVideos() {
    for ( var i = 0; i < numberOfClusters; i++) {
       videoAddress[i] = [];
       videoAddressReady[i] = false;
       $.ajax({
               method:"POST",
               url:"/getVideo",
               data: {
                  id :        uID,
                  clusterId : i
               },
               success: function(result) {
                    // first element of videoAddress is the cluster id
                    var split = result.split(" ");
                    var clusterId = split[0];
                    for ( var j = 1; j < split.length; j++) {
                        videoAddress[clusterId][j - 1] = split[j];
                    }
                    videoAddressReady[clusterId] = true;
               }
           });
    }

    // have to wait since ajax is asynchronous
    setTimeout(checkIfVideosAreReady, timeOutTime);
}


function readLog() {
    $.ajax({
        method:"POST",
        url:"/log",
        data: {
           id : uID
        },
        success: function(result) {
            if(result.endsWith("FINISH<br>")) {
              isLoading = false;
              isFinished = true;
            }
            $("#panel").html(result);
        }
    });
    if(isLoading) {
        setTimeout(readLog, timeOutTime);
    } else if(isFinished){
        $("#log").slideToggle();
        getVideos();
    }
}

function sendRequest() {
    numberOfClusters = $("#kcluster").val();

    $.ajax({
      method:"POST",
      url: "/input",
      data: {
        file:     $("#baseFolder").val(),
        video:    $("#videoExtension").val(),
        heat:     $("#heat").val(),
        entropy:  $("#entropy").val(),
        knn:      $("#knn").val(),
        kcluster: $("#kcluster").val(),
        distance: $("#distance").val(),
        out:      $("#outFolder").val(),
        time:     $("#time").val(),
        id:       uID
      },
      success: function( result ) {
        $("#container").slideToggle();
        $("#log").slideToggle();
        $("#panel").text(result);
        isLoading = true;
        setTimeout(readLog, timeOutTime);
      }
    });
}

function cancelFunction() {
    $("#log").slideToggle();
    $("#container").slideToggle();
    isLoading = !isLoading;
    uID = Math.random() * new Date().getTime();
}

$("#submit").click(sendRequest);
$("#cancel").click(cancelFunction);