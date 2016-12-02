var uID = Math.random() * new Date().getTime();
var index = -1;
var timeOutTime = 100;


function getChat() {
    $.ajax({
        method:"POST",
        url:"/chat",
        data: {
           id : uID,
           index : index
        },
        success: function(result) {
            var chat = JSON.parse(result);
            setTimeout(getChat, timeOutTime);
        }
    });

}

function inputKeyPress(event) {
    if("Enter" === event.code && !event.shiftKey) {
        console.log(event);
    }
}

$("#clear").click(function() { console.log("log clear")});

getChat();