var uID = "id" + (Math.random() * new Date().getTime());
var index = -1;
var timeOutTime = 100;


function generateNotification(title, text) {
    var notification = new Notification(title, { body: text});
}

function getChat() {
    $.ajax({
        method:"POST",
        url:"/chat",
        data: {
           id : uID,
           index : index
        },
        success: function(result) {
            var pattern = new RegExp('^(https?:\/\/)');
            var chat = JSON.parse(result);
            if(chat.needClean) {
                $("#chat").empty();
                index = -1;
            } else {
                $("#numberOfUsers").html(chat.users.length);
                $("#userNames").empty()
                for(var i = 0; i < chat.users.length; i++) {
                    $("#userNames").append("<li>" + chat.users[i] + "</li>");
                }
                for(var i = 0; i < chat.log.length; i++) {
                    // replace new line in http (%0A) by new line in HTML (<br />) then  decode http and replace spaces in http(+) by a space char
                    var text = decodeURIComponent(chat.log[i].text.replace(new RegExp("%0A", "g"),"<br />")).replace(/\+/g,  " ");
                    var id = decodeURIComponent(chat.log[i].id);
                    if(pattern.test(text)) {
                        $("#chat").append("<p>" + id + " > <a target='_blank' href='" + text +  "'>" + text + "</a></p>");
                    } else {
                        $("#chat").append("<p>" + id + " > " + text + "</p>");
                    }
                    if(id !== uID) {
                        generateNotification("PublicChat", id + " > " + text);
                    }
                }
                index += chat.log.length;
            }

            setTimeout(getChat, timeOutTime);
        }
    });
}

function sendText() {
    var text = $("#input").val();
    //send request to server
    $.ajax({
            method:"POST",
            url:"/putText",
            data: {
               id : uID,
               log : text
            },
            success: function(result) {
                if("OK" === result) {
                    $("#input").val("");
                }
            }
        });
}

function inputKeyPress(event) {
    if("Enter" === event.code && !event.shiftKey) {
        sendText();
    }
}

function hideIfMobile() {
    if( /Android|webOS|iPhone|iPad|iPod|BlackBerry|IEMobile|Opera Mini/i.test(navigator.userAgent) ) {
        $("#shiftPlusEnterP").hide();
    }else {
        $("#send").hide();
    }
}

function clearServer() {
        $.ajax({
                method:"POST",
                url:"/clear",
                data: {
                   id : uID
                },
                success: function(result) {
                    if("OK" === result) {
                        console.log("console clear");
                    }
                }
            });
}

$("#clear").click(clearServer);
$("#send").click(sendText);
$("#changeNameButton").click(function() { uID = $("#myIdIn").val() });
$("#myIdIn").val(uID);


hideIfMobile();
getChat();

document.addEventListener('DOMContentLoaded', function () {
  if (Notification.permission !== "granted")
    Notification.requestPermission();
});