<!DOCTYPE html>
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
    <div class="loading">Loading&#8230;</div>
    <div class='col-6  wrap '>
        <header>
            <h3>주문/결제</h3>
            <button type='button'><a href="./MixedPaid.html"><i class="fa-solid fa-x"></i></a></button>
        </header>
        <div class='col-12 d-flex align-items-center flex-column align-items-center moduleBody'>

            <div class="col-12" id="accordionExample">
                <div class="accordion-item">
                    <h3 class="accordion-header" id="headingOne">
                        <button class="accordion-button justify-content-between align-items-center" type="button"
                                data-bs-toggle="collapse" data-bs-target="#collapseOne" aria-expanded="true"
                                aria-controls="collapseOne" onclick="detailView()">
                            <div class='d-flex justify-content-between w-100 first'>
                                <h3>결제상품</h3> <div><span id="toatalamount" th:text="${paymentInfo.totalAmount} + '원'"> </span><i class="fa-solid fa-angle-down" style="margin-left: 5px"></i></div>
                            </div>
                        </button>
                    </h3>
                    <div id="collapseOne" class="accordion-collapse collapse" aria-labelledby="headingOne"
                         data-bs-parent="#accordionExample">
                        <div class="accordion-body mixedPaid">
                            <div class='d-flex justify-content-between second'>
                                <div class='d-flex justify-content-between'>
                                    <p>항공</p>
                                    <span th:text="${paymentInfo.poductTypeFAmount + '원'}"></span>
                                </div>
                                    <div class='d-flex justify-content-between detail' th:each="item : ${paymentInfo.poductTypeF}">
                                        <p  th:text="${'-' + item.name}"></p> <p th:text="${#numbers.formatInteger(item.amount, 3, 'COMMA')+ '원'}"></p>
                                    </div>
                            </div>

                        </div>
                        <div class="accordion-body">
                            <div class='d-flex justify-content-between second'>
                                <div class='d-flex justify-content-between'>
                                    <p>숙박</p>
                                    <span th:text="${paymentInfo.poductTypeHAmount + '원'}"></span>
                                </div>
                                <div class='d-flex justify-content-between detail' th:each="item : ${paymentInfo.poductTypeH}">
                                    <p  th:text="${'-' + item.name}"></p> <p th:text="${#numbers.formatInteger(item.amount, 3, 'COMMA')+ '원'}"></p>
                                </div>
                            </div>
                        </div>
                        <div class="accordion-body">
                            <div class='d-flex justify-content-between second'>
                                <div class='d-flex justify-content-between'>
                                    <p>열차</p>
                                    <span th:text="${paymentInfo.poductTypeTAmount+ '원'}"></span>
                                </div>
                                <div class='d-flex justify-content-between detail' th:each="item : ${paymentInfo.poductTypeT}">
                                    <p  th:text="${'-' + item.name}"></p> <p th:text="${#numbers.formatInteger(item.amount, 3, 'COMMA')+ '원'}"></p>
                                </div>
                            </div>
                        </div>
                        <div class='d-flex justify-content-between align-items-center last'>
                            <h4>총 상품금액</h4><span th:text="${paymentInfo.totalAmount} + '원'"></span>
                        </div>
                    </div>
                </div>
            </div>
            <div class='col-12 bzpDiv' th:if="${paymentInfo.totalPointAmount2 > 0}">
                <div class="first">
                    <h3>포인트 사용</h3>
                </div>
                <div class='d-flex justify-content-between second'>
                    <div class='d-flex justify-content-between'>
                        <p>항공</p><span th:text="${paymentInfo.poductTypeFPointAmount+ '원'}"></span>
                    </div>
                    <div class='d-flex justify-content-between'>
                        <p>숙박</p><span th:text="${paymentInfo.poductTypeHPointAmount + '원'}"></span>
                    </div>
                    <div class='d-flex justify-content-between'>
                        <p>열차</p><span th:text="${paymentInfo.poductTypeTPointAmount + '원'}"></span>
                    </div>


                </div>
                <div class='d-flex justify-content-between align-items-center last'>
                    <h4>총 포인트 사용금액</h4><span th:text="${paymentInfo.totalPointAmount} + '원'"></span>
                </div>
            </div>
            <div class='col-12 bzpDiv'>
                <div class="first">
                    <h3>결제금액</h3>
                </div>
                <div class='second'>
                    <div class='d-flex justify-content-between'>
                        <p>상품금액</p><span th:text="${paymentInfo.totalAmount} + '원'"></span>
                    </div>
                    <div class='d-flex justify-content-between' th:if="${paymentInfo.totalPointAmount2 > 0}">
                        <p>포인트사용</p><span th:text="'-' + ${paymentInfo.totalPointAmount} + '원'"></span>
                    </div>
                </div>

                <div class='d-flex justify-content-between align-items-center last'>
                    <h4>총 결제 금액</h4><span th:text="${paymentInfo.pgtotalAmount} + '원'"></span>
                </div>
            </div>
            <div class='col-12 bzpDiv'>
                <div class='first'>
                    <h3>결제수단</h3>
                </div>
                <div>
                    <label class="custom-radio"> <input type="radio" checked> 일반 카드결제</label>
                </div>
            </div>
        </div>
        <div class='col-12 button'>
            <button type='button' onclick="submiton()">
                <span>결제하기</span>

            </button>
        </div>
    </div>
</main>
<!-- Modal -->
<div class="modal fade" id="exampleModal" tabindex="-1" aria-labelledby="exampleModalLabel" aria-hidden="true">
    <div class="modal-dialog modal-dialog-centered">
        <div class="modal-content">
            <div class="modal-body">
                <h4>포인트 미등록 사용자입니다.</h4>
                <p>등록 후 다시 이용해주세요</p>
            </div>
            <div class="modal-footer">
                <button type="button" class="btn btn-primary" data-bs-dismiss="modal">확인</button>
            </div>
        </div>
    </div>
</div>

<script src="bootstrap-5.3.3-dist/js/bootstrap.bundle.js"></script>
<script src="https://kit.fontawesome.com/6a17766ce6.js" crossorigin="anonymous"></script>
<script src="js/bizscript.js"></script>
<script>

    var spinner = document.querySelector('.loading');
    spinner.style.display = 'none';


    function detailView(){
        const addBtn = document.querySelector(".accordion-button");
        const angleDown = document.querySelector(".fa-angle-down");
        if (addBtn.classList.contains("collapsed")) {
            document.getElementById("toatalamount").style.color = "#4286F2";
            angleDown.style.transform = "rotate(0deg)";
        }else{
            document.getElementById("toatalamount").style.color = "#FFF";
            angleDown.style.transform = "rotate(180deg)";
        }
    }
    function submiton(){

        spinner.style.display = 'block'; // spinner 보이기
        var screenwidth = screen.availWidth;
        var deviceType = "pc";
        if(767 > screenwidth){
            deviceType = "mobile";
        }
        var result = {
            payinfo: "[[${paymentInfoEnc}]]",
            payMethodTypeCode: "11",
            deviceType: deviceType
        }
        const option = {
            method : 'POST',
            headers:{
                'Content-Type' : 'application/json'
            },
            body: JSON.stringify(result)
        };

        fetch("/biz_payment/payreadyset", option)
            .then(response => response.json())
            .then(response => {
                if(response.code === "0000"){
                    window.open(response.url,"_self" ,"width=500, height=500");
                } else {
                    window.open(response.url + "?code=" + response.code + "&msg=" +  response.msg,"_self" ,"width=500, height=500");
                }

            })
    };
</script>
</body>
</html>