<!DOCTYPE html>
<html lang="UTF-8">
<html xmlns:th=http://www.thymeleaf.org>
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>BizPatment</title>
    <link rel="stylesheet" href="css/bizstyle.css">
    <link rel="stylesheet" href="bootstrap-5.3.3-dist/css/bootstrap.min.css">
    <link rel="stylesheet" href="https://cdn.rawgit.com/moonspam/NanumSquare/master/nanumsquare.css">
</head>
<body>
<main class="col-12">
    <div class='col-6  wrap '>
        <header>
            <h3>주문/결제</h3>
            <button type='button'><a href="./MixedPaid.html"><i class="fa-solid fa-x"></i></a></button>
        </header>
        <div class='col-12 d-flex align-items-center flex-column align-items-center moduleBody paidDiv'>
            <div class='aa'>
                <div class='resultYes'>
                    <div>
                        <i class="fa-solid fa-exclamation"></i> </div>
                    <h2>결제를 진행할 수 없습니다</h2>
                    <p th:text="${msg}"></p>
                        <span th:text="${ '오류코드: ' + code}"></span>
                </div>
            </div>

        </div>
        <div class='col-12 button'>
                <button type='button' onclick="doclose()">
                    <span>확인</span>
                </button>
        </div>
    </div>
</main>
<script src="bootstrap-5.3.3-dist/js/bootstrap.bundle.js"></script>
<script src="https://kit.fontawesome.com/6a17766ce6.js" crossorigin="anonymous"></script>
<script src="js/bizscript.js"></script>
</body>
</html>