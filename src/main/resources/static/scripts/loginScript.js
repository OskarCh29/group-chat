$(document).ready(function () {
    $("#loginForm").submit(function (e) {
        e.preventDefault();

        const loginData = {
            username: $("#username").val(),
            password: $("#password").val()
        }

        $.ajax({
            type: "POST",
            url: "http://localhost:8080/api/user",
            contentType: "application/json",
            data: JSON.stringify(loginData),
            success: function (response) {

                sessionStorage.setItem("userId", response.userId);
                sessionStorage.setItem("token", response.token);
                window.location.href = "/chat.html";


            },
            error: function (error) {
                alert(error.responseJSON ? error.responseJSON.message : error.responseText);
                
            }
        });
    })
    $("#signIn").click(function (e) {
        e.preventDefault();
        window.location.href = "/signInPanel.html";
    });
    $("#forgotPassword").click(function (e){
        e.preventDefault();
        window.location.href = "/forgotPassword.html"
    })
    
});

