$(document).ready(function () {

    $("#backButton").click(function (e) {
        e.preventDefault();
        window.location.href = "/index.html";
    });

    $("#signInForm").submit(function (e) {
        e.preventDefault();

        const pass = $("#password").val();
        const repPass = $("#repPassword").val();
        const email = $("#email").val();

        if (pass !== repPass) {
            alert("Passwords do not match");
            return;
        }
        if (!validateEmail(email)) {
            alert("Incorrect e-mail type");
            return;
        }
        if (!validatePasswordStrength(pass)){
            alert("Password must have one number, one capital letter and 6 characters length");
            return;
        }

        const newUser = {
            username: $("#username").val(),
            password: pass,
            email: email,
            token: null
        };
        $.ajax({
            type: "POST",
            contentType: "application/json",
            url: "http://localhost:8080/api/newUser",
            data: JSON.stringify(newUser),
            success: function (response) {
                alert("New User created. E-mail confirmation required");
                window.location.href = "/index.html"
            },
            error: function (error) {
                alert(error.responseText);
            }

        });

    })
});
function validateEmail(email) {
    const emailPattern = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
    return emailPattern.test(email);
}
function validatePasswordStrength(password){
    if(password.length <= 6){
        return false;
    }
    if(!/[A-z]/.test(password)){
        return false;
    }
    if(!/[0-9]/.test(password)){
        return false;
    }
    return true;
}