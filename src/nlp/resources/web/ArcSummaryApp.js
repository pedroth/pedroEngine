var numberOfClusters;
var isLoading = true;
var timeOutTime = 100;
var uID = Math.random();

function buildVideos() {
    for ( var i = 0; i < numberOfClusters; i++) {
       $.ajax({
               method:"POST",
               url:"/getVideo",
               data: {
                  id :        uID,
                  clusterId : i
               },
               success: function(result) {
                    // first element of videoAddress is the cluster id
                    var videoAddress = result.split(" ");
                    var div = document.createElement('div');
                    var clusterId = videoAddress[0];
                    div.id = "cluster" + clusterId;
                    div.class = "form-group row";
                    $("body").append(div);
                    for (var j = 1; j < videoAddress.length; j++) {
                        var videoAddressUrl = encodeURIComponent(videoAddress[j]);
                        var videoTag = "<video width='320' height='240' controls>" +
                                           "<source src='summary" + uID + "/" + clusterId + "/" + videoAddressUrl + "' type='video/mp4'>" +
                                           "Your browser does not support the video tag."+
                                       "</video>"
                        $("#" + div.id).append(videoTag);
                    }
               }
           });
    }
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
            }
            $("#panel").html(result);
        }
    });
    if(isLoading) {
        setTimeout(readLog, timeOutTime);
    } else {
        $("#log").slideToggle();
        buildVideos();
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
        setTimeout(readLog, timeOutTime);
      }
    });
}

$("#submit").click(sendRequest);
$("#cancel").click(function() {$("#log").slideToggle(); $("#container").slideToggle();  isLoading = !isLoading ? true : false;});