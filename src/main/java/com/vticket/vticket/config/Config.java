package com.vticket.vticket.config;

public class Config {
    public static final class LINK_ACCOUNT_TYPE {
        public static final String FACEBOOK = "facebook";
        public static final String GOOGLE = "google";
        public static final String APPLE = "apple";
        public static final String PHONE = "phone";
    }

    public static final class SEND_OTP_TYPE {

        public static final String SMS = "sms";
        public static final String EMAIL = "email";
        public static final String SMSEMAIL = "smsandemail";
    }

    public static final class LOGIN_TYPE {

        public static final String FACEBOOK = "facebookId";
        public static final String GOOGLE = "googleId";
        public static final String DEVIDE = "deviceId";
        public static final String APPLE = "appleId";
        public static final String PHONE = "phone";
        public static final String EMAIL = "email";
        public static final String USERNAME = "user";
        public static final String USERID = "userId";
        public static final String PARTNERID = "partnerId";
        public static final String OPENAPP = "userIdApp";
    }

    public static final class LOGIN_TYPE_PC {

        public static final String FACEBOOK = "4";
        public static final String GOOGLE = "8";
        public static final String APPLE = "10";
        public static final String PHONE = "phone";
        public static final String EMAIL = "email";
        public static final String USERNAME = "user";
        public static final String USERID = "userId";
        public static final String PARTNERID = "partnerId";
    }

    public static final class RABBITMQ {
        // MAIL MODULE
        public static final String QUEUE_MAIL = "mail.queue";
        public static final String EXCHANGE_MAIL = "mail.exchange";
        public static final String ROUTING_MAIL = "mail.routing";

        // PAYMENT MODULE
        public static final String QUEUE_PAYMENT = "payment.queue";
        public static final String EXCHANGE_PAYMENT = "payment.exchange";
        public static final String ROUTING_PAYMENT = "payment.routing";

        // TICKET MODULE
        public static final String QUEUE_TICKET = "ticket.queue";
        public static final String EXCHANGE_TICKET = "ticket.exchange";
        public static final String ROUTING_TICKET = "ticket.routing";
    }

    public static final class CODE {

        public static final String STAMP_KEY = "stamp";
        public static final String ERROR_CODE_KEY = "errorCode";
        public static final String SUCCESS_CODE_KEY = "success";
        public static final String CODE_KEY = "code";
        public static final int SUCCESS_CODE = 200;
        public static final int ERROR_CODE = 500;
        public static final int BLACK_CODE = 6969;
        public static final int WHITE_CODE = 7000;
        public static final int ERROR_CODE_0 = 0;//Thành công
        public static final int ERROR_CODE_99 = -99;//Lỗi không xác định
        public static final int ERROR_CODE_45 = -45;//Tài khoản bị block bởi admin
        public static final int ERROR_CODE_46 = -46;//Tên tài khoản đã tồn tại
        public static final int ERROR_CODE_48 = -48;//Tài khoản bị block
        public static final int ERROR_CODE_50 = -50;//Tài khoản không tồn tại hoặc không đúng
        public static final int ERROR_CODE_51 = -51;//Tài khoản không có số điện thoại bảo vệ
        public static final int ERROR_CODE_52 = -53;//Tài khoản không có email bảo vệ
        public static final int ERROR_CODE_53 = -53;//Mật khẩu không đúng
        public static final int ERROR_CODE_54 = -54;//Tài khoản đã nâng cấp vui lòng đăng nhập với username
        public static final int ERROR_CODE_102 = -102;//Phiên đăng nhập không hợp lệ
        public static final int ERROR_CODE_103 = -103;//Phiên đăng nhập hết hạn
        public static final int ERROR_CODE_200 = -200;//Quá nhiều request, quy định hiện tại: (> 30 request / 10 giây đối với 1 AccessToken sẽ trả về lỗi này > 10 lỗi này trong 60 giây sẽ bị block trong 5 phút, vẫn trả về mã lỗi này)
        public static final int ERROR_CODE_201 = -201;//Timeout
        public static final int ERROR_CODE_213 = -213;//RefreshToken không hợp lệ
        public static final int ERROR_CODE_214 = -214;//RefreshToken hết hạn
        public static final int ERROR_CODE_217 = -217;//Chữ ký không chính xác
        public static final int ERROR_CODE_218 = -218;//RequestTime không hợp lệ
        public static final int ERROR_CODE_220 = -220;//ClientIp không hợp lệ
        public static final int ERROR_CODE_222 = -222;//ClientKey không hợp lệ
        public static final int ERROR_CODE_223 = -223;//ClientSecret không hợp lệ
        public static final int ERROR_CODE_505 = -505;//Sai định dạng Username
        public static final int ERROR_CODE_506 = -506;//Sai định dạng Email
        public static final int ERROR_CODE_507 = -507;//Sai định dạng Mobile
        public static final int ERROR_CODE_510 = -510;//Mã OTP không chính xác
        public static final int ERROR_CODE_511 = -511;//Mã xác thực không chính xác
        public static final int ERROR_CODE_512 = -512;//Số lần lấy lại mật khẩu qua mobile vượt quá số lượng cho phép
        public static final int ERROR_CODE_513 = -513;//Số lần xác thực qua mobile vượt quá số lượng cho phép
        public static final int ERROR_CODE_514 = -514;//Sai số điện thoại cũ
        public static final int ERROR_CODE_515 = -515;//Tên đầy đủ không hợp lệ
        public static final int ERROR_CODE_516 = -516;//Ngày sinh không hợp lệ
        public static final int ERROR_CODE_517 = -517;//Ảnh đại diện (Base64string) không đúng
        public static final int ERROR_CODE_518 = -518;//Sai password cũ
        public static final int ERROR_CODE_519 = -519;//Mật khẩu mới trùng với mật khẩu cũ
        public static final int ERROR_CODE_520 = -520;//Tài khoản chưa được xác thực
        public static final int ERROR_CODE_521 = -521;//OTP đã được sử dụng
        public static final int ERROR_CODE_522 = -522;//Tài khoản đã được xác thực
        public static final int ERROR_CODE_523 = -523;//Số chứng minh/Hộ chiếu đã có
        public static final int ERROR_CODE_524 = -524;//OTP không tồn tại
        public static final int ERROR_CODE_525 = -525;//Tên tài khoản mới chưa nhập
        public static final int ERROR_CODE_526 = -526;//Chỉ thay đổi tên tài khoản một lần
        public static final int ERROR_CODE_527 = -527;//Email không chính xác
        public static final int ERROR_CODE_528 = -528;//OTP cho số điện thoại cũ không chính xác
        public static final int ERROR_CODE_529 = -529;//OTP cho số điện thoại mới không chính xác
        public static final int ERROR_CODE_530 = -530;//Bạn đã yêu cầu gửi OTP quá 5 lần. Vui lòng thử lại sau 30 phút
        public static final int ERROR_CODE_531 = -531;//Bạn đã nhập sai OTP quá 5 lần. Vui lòng thử lại sau 30 phút
        public static final int ERROR_CODE_532 = -532;//Kiểu kết nối không chính xác
        public static final int ERROR_CODE_533 = -533;//Số điện thoại trùng với số điện thoại đã xác thực
        public static final int ERROR_CODE_534 = -534;//Email trùng với số email đã xác thực
        public static final int ERROR_CODE_535 = -535;//Hệ thống chưa hỗ trợ tính năng OTP cho số điện thoại này
        public static final int ERROR_CODE_601 = -601;//Số CMTND/Hộ chiếu không đúng.
        public static final int ERROR_CODE_602 = -602;//Ngày cấp CMTND/Hộ chiếu không đúng.
        public static final int ERROR_CODE_603 = -603;//Tên tài khoản không để trống.
        //public static final int ERROR_CODE_99	= -99 ;public static final String ERROR_DESC_99	  = ""  ;//Cập nhật dữ liệu không thành công.
        public static final int ERROR_CODE_604 = -604;//Không lưu được dữ liệu vào Cache.
        public static final int ERROR_CODE_605 = -605;//Tên tài khoản hoặc mật khẩu không để trống.
        public static final int ERROR_CODE_606 = -606;//Không để trống địa chỉ nhận mã OTP.
        public static final int ERROR_CODE_666 = -666;//Tài khoản không có quyền thực hiện tính năng này.
        public static final int ERROR_CODE_607 = -607;//Số điện thoại không để trống.
        public static final int ERROR_CODE_608 = -608;//Mã OTP mới không chính xác
        public static final int ERROR_CODE_609 = -609;//Mã OTP cũ không chính xác
        public static final int ERROR_CODE_709 = -709; //AccessToken của Apple không chính xác
        public static final int ERROR_CODE_710 = -710;//AccessToken của Facebook không chính xác
        public static final int ERROR_CODE_711 = -711;//AccessToken của Google không chính xác
        public static final int ERROR_CODE_712 = -712;//Tài khoản đang liên kết với tài khoản fb trước đó
        public static final int ERROR_CODE_713 = -713;//Tài khoản face đã được kết nối với tài khoản khác
        public static final int ERROR_CODE_714 = -714;//Tài khoản google đã được kết nối với tài khoản khác
        public static final int ERROR_CODE_715 = -715;//Tài khoản đang liên kết với tài khoản gg trước đó
        public static final int ERROR_CODE_716 = -716;//số điện thoại đang liên kết với tài khoản khác
        public static final int ERROR_CODE_717 = -717;//Tài khoản chưa liên kết với facebook
        public static final int ERROR_CODE_718 = -718;//Tài khoản chưa liên kết với google
        public static final int ERROR_CODE_719 = -719;//Tài khoản chưa liên kết với số điện thoại
        public static final int ERROR_CODE_999 = -999;//Có lỗi trong quá trình xử lý.
        public static final int ERROR_CODE_1001 = -1001;//đơn hàng đã bị hủy
        public static final int ERROR_CODE_1002 = -1002;//đơn hàng đang chờ xử lý
        public static final int ERROR_CODE_2000 = -2000;//dupicate key
        public static final int ERROR_CODE_720 = -720;//Bạn đã nhập password quá 5 lần. Vui lòng thử lại sau 10p
        public static final int ERROR_CODE_721 = -721;//tài khoản của bạn đang bị khóa
        public static final int ERROR_CODE_722 = -722;//captcha expired
        public static final int ERROR_CODE_723 = -723;//bạn đã đăng nhập sai nhiều lần vui lòng thử lại sau 10'
        public static final int ERROR_CODE_725 = -725;//captcha invalid
        public static final int ERROR_CODE_838 = -838;//Vui long su dung 3G/4G Viettel
        public static final int ERROR_CODE_839 = -839;//return url không đúng
        public static final int ERROR_CODE_888 = -888;//data request invalid
    }


}
