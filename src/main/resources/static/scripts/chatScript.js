
$(document).ready(function () {
    connect();
    loadMessages();
    sendMessage();
});

let stompClient = null;

function connect() {
    var socket = new SockJS('http://localhost:8080/chat');
    stompClient = Stomp.over(socket);

    stompClient.connect({}, function (frame) {
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
        if (messageBody === "") {
            alert("Message empty");
            return;
        }

        const userId = sessionStorage.getItem("userId");
        const token = sessionStorage.getItem("token");
        if (!token) {
            alert("Authorization token missing");
            return;
        }
        const message = {
            messageBody: messageBody
        };
        const rawToken = getRawToken(userId, token);

        $.ajax({
            type: "POST",
            url: "http://localhost:8080/chat/message",
            contentType: "application/json",
            data: JSON.stringify(message),
            headers: { "Authorization": rawToken },
            success: function (response) {
                $("#textbox").val("");
            },
            error: function (error) {
                console.error("Error while sending message", error);
                alert("Error while sending message");
            }
        })
    })
}
function loadMessages() {
    $.ajax({
        type: "GET",
        url: "http://localhost:8080/chat",
        dataType: "json",
        success: function (response) {
            $("#msgBox").empty();

            response.forEach(function (message) {
                $("#msgBox").append(message.createdAt + ": " + message.username + ": " + message.messageBody + "<br>");
            });
        },
        error: function (error) {
            alert("Error while loading messages");
        }
    });
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