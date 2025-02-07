
$(document).ready(function () {
    loadMessage();
    sendMessage();
});
setInterval(loadMessage, 3000);
function loadMessage() {
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
            console.error(error);
            alert("Error while loading messages");
        }

    });
};
function sendMessage() {
    $("#sendMessage").click(function (e) {
        e.preventDefault();
        const userId = sessionStorage.getItem("userId");
        const token = sessionStorage.getItem("token");

        const rawToken = getRawToken(userId, token);

        const message = {
            messageBody: $("#textbox").val(),
            userId: userId,
            token: token

        };
        $.ajax({
            type: "POST",
            url: "http://localhost:8080/chat/message",
            contentType: "application/json",
            data: JSON.stringify(message),
            headers: {
                "Authorization": rawToken
            },
            success: function () {
                $("#textbox").val("");
                loadMessage();
            },
            error: function (error) {
                console.error(error);
                alert("Error while sending message");

            }
        });
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
        data: {userId: userId},
        success: function () {
            window.location.href = "/index.html"
        },
        error: function(error){
            console.error(error);
            alert("External error while logging out");
        }
    })


});