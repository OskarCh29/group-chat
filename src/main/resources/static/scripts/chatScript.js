
$(document).ready(function () {
    connect();
    loadMessages();
    sendMessage();
});

let socket = null;
let stompClient = null;

let isConnected = false;

function setConnected(connected) {
    isConnected = connected;
    if (connected) {
        $("#status").text("Connected");
    } else {
        $("#status").text("Disconnected");
    }
}


function loadMessages(){
    $.ajax({
        type: "GET",
        url: "http://localhost:8080/messages",
        dataType: "json",
        success: function (response) {
            $("#msgBox").empty();

            response.forEach(function(message) {
                $("#msgBox").append(message.createdAt + ": " + message.username + ": " + message.messageBody + "<br>");
            });
        },
        error: function(error){
            alert("Error while loading messages");
        }
    });
}
function connect() {
    socket = new SockJS('http://localhost:8080/chat');
    stompClient = Stomp.over(socket);

    stompClient.connect({}, function (frame) {
        setConnected(true);
        console.log("Connected: " + frame);
        stompClient.subscribe('/topic/chat', function (messageOutput) {
            const message = JSON.parse(messageOutput.body);
            displayMessage(message);
        });
    });
}
function displayMessage(message) {
    $("#msgBox").append(message.createdAt + ": " + message.username + ": " + message.messageBody + "<br>");
}
function sendMessage() {
    $("#sendMessage").click(function (e) {
        e.preventDefault();

        const messageBody = $("#textbox").val().trim();
        if(messageBody ===""){
        alert("Message empty");
        return;
        }

        const userId = sessionStorage.getItem("userId");
        const token = sessionStorage.getItem("token");
        const message = {
            messageBody: $("#textbox").val()
        };

        const rawToken = getRawToken(userId, token);

        stompClient.send("/app/chat", { "Authorization": rawToken }, JSON.stringify(message));
        $("#textbox").val("")
    })
}
function getRawToken(userId, token) {
    const rawToken = `${userId}:${token}`;
    return btoa(rawToken);
}
$("#logout").click(function (e) {
    e.preventDefault()
    const userId = sessionStorage.getItem("userId");
    $.ajax({
        type: "PUT",
        url: "http://localhost:8080/api/user",
        data: { userId: userId },
        success: function () {
            window.location.href = "/index.html"
        },
        error: function (error) {
            console.error(error);
            alert("External error while logging out");
        }
    })
});