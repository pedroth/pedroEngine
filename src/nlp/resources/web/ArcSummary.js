var numberOfClusters;
var isLoading = true;
var timeOutTime = 100;
var uID = Math.random();


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