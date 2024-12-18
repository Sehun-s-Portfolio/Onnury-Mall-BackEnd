<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <title>Insert title here</title>
</head>
<body>
${url}
<button onclick="test()">ssss</button>
</body>
<script>

    var rr = {
        "pointMid":"435353",
        "tno": "55555",
        "merchantOrderDt":"20201101",
        "merchantUserKey":"CI1234567890",
        "productName":"티웨이 필리핀 포함 총2건",
        "merchantUserNm":"홍길동",
        "merchantTotalOrderID":"demo_order_no_684839495",
        "returnUrl": "https://www.naver.com",
        "merchantUserTel":"01012341234",
        "hubOrgId":"123213123123123",
        "quantity":2,
        "totalAmount":2500,
        "productItems":[
            {
                "seq":"1",
                "productType":"F",
                "merchantOrderID":"demo_order_no_684839495",
                "biz_no":"1234567890",
                "name":"티웨이 필리핀 ",
                "count":2,
                "amount":1000,
                "taxFreeAmount":0,
                "vatAmount":0,
            },
            {
                "seq":"2",
                "productType":"H",
                "biz_no":"1234567890",
                "merchantOrderID":"demo_order_no_684839495",
                "name":"파라다이스부산 ",
                "count":1,
                "amount":500,
                "taxFreeAmount":0,
                "vatAmount":0,
            }
        ],
        "complexYn":"Y",
        "pgtotalAmount": 1000,
        "PgMid":"45435",
    };


    function test(){
        const option = {
            method : 'POST',
            headers:{
                'Content-Type' : 'application/json'
            },
            body: JSON.stringify(rr)
        };

        fetch("/biz_payment/payreadyset2", option)
            .then(response => response.json())
            .then(response => {
                //window.open(response.url, "_blank", "width=500, height=500");
            })

    }

</script>
</html>