var isLoading = true;
var timeOutTime = 100;
var uID = Math.random();

function readLog() {
    $.ajax({
        url:"/log",
        data: {
           id : uID
        },
        success: function(result) {
            if(result.endsWith("<FINISH><br>")) {
              $("#panel").html("Finish");
              isLoading = false;
            }else {
              $("#panel").html(result);
            }
        }
    });
    if(isLoading) {
        setTimeout(readLog, timeOutTime);
    }
}

function sendRequest() {
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
$("#previousSettings").click(function() {$("#log").slideToggle(); $("#container").slideToggle();});