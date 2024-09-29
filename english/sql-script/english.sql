CREATE DATABASE  IF NOT EXISTS `english`;
USE `english`;

CREATE TABLE `User` (
    `user_id` INTEGER NOT NULL AUTO_INCREMENT UNIQUE COMMENT 'Khóa chính để xác định duy nhất mỗi người dùng.',
    `email` VARCHAR(255) NOT NULL UNIQUE COMMENT 'Địa chỉ email của người dùng, thường dùng để đăng nhập.',
    `password` VARCHAR(255) NOT NULL COMMENT 'Mật khẩu đã được mã hóa để bảo mật.',
    `enable` tinyint(1) DEFAULT 1,
    `subscription_plan` ENUM('none', '6_months', '1_year', '3_years') DEFAULT 'none' COMMENT 'Gói dịch vụ mà người dùng đã đăng ký.',
    `subscription_start_date` DATETIME COMMENT 'Ngày bắt đầu gói dịch vụ.',
    `subscription_end_date` DATETIME COMMENT 'Ngày kết thúc gói dịch vụ, dùng để xác định khi nào người dùng cần đăng ký lại.',
    `role` ENUM('ROLE_user', 'ROLE_admin') NOT NULL DEFAULT 'ROLE_user' COMMENT 'Xác định vai trò của người dùng.',
    PRIMARY KEY(`user_id`)
);

CREATE TABLE `Course` (
    `course_id` INTEGER NOT NULL AUTO_INCREMENT UNIQUE COMMENT 'Khóa chính định danh khóa học.',
    `course_name` VARCHAR(255) NOT NULL COMMENT 'Tên khóa học.',
    `description` TEXT NOT NULL COMMENT 'Mô tả chi tiết về khóa học.',
    PRIMARY KEY(`course_id`)
);

CREATE TABLE `Topic` (
    `topic_id` INTEGER NOT NULL AUTO_INCREMENT UNIQUE COMMENT 'Khóa chính định danh chủ đề.',
    `topic_name` VARCHAR(255) COMMENT 'Tên của chủ đề (ví dụ: Nghề nghiệp, Tính cách).',
    `course_id` INTEGER COMMENT 'Khóa ngoại tham chiếu đến bảng Course.',
    `description` TEXT COMMENT 'Mô tả chi tiết về chủ đề.',
    `order` INTEGER COMMENT 'Xác định thứ tự của chủ đề trong khóa học.',
    PRIMARY KEY(`topic_id`),
    FOREIGN KEY (`course_id`) REFERENCES `Course`(`course_id`) ON UPDATE NO ACTION ON DELETE CASCADE
);

CREATE TABLE `Vocabulary` (
    `vocab_id` INTEGER NOT NULL AUTO_INCREMENT UNIQUE COMMENT 'Khóa chính định danh từ vựng.',
    `word` VARCHAR(255) COMMENT 'Từ vựng (ví dụ: "doctor", "teacher").',
    `meaning` TEXT COMMENT 'Định nghĩa của từ.',
    `example_sentence` TEXT COMMENT 'Ví dụ về câu chứa từ.',
    `topic_id` INTEGER COMMENT 'Khóa ngoại tham chiếu đến bảng Topic.',
    `pronunciation` TEXT COMMENT 'Phát âm.',
    PRIMARY KEY(`vocab_id`),
    FOREIGN KEY (`topic_id`) REFERENCES `Topic`(`topic_id`) ON UPDATE NO ACTION ON DELETE CASCADE
);

CREATE TABLE `User_Progress` (
    `progress_id` INTEGER NOT NULL AUTO_INCREMENT UNIQUE COMMENT 'Khóa chính định danh mỗi bản ghi tiến trình.',
    `user_id` INTEGER COMMENT 'Khóa ngoại tham chiếu đến bảng User.',
    `vocab_id` INTEGER COMMENT 'Khóa ngoại tham chiếu đến bảng Vocabulary.',
    `review_interval` INTEGER COMMENT 'Khoảng thời gian lặp lại ngắt quãng.',
    `last_reviewed` DATETIME COMMENT 'Thời điểm lần ôn tập gần nhất.',
    `next_review` DATETIME COMMENT 'Thời điểm người dùng cần ôn lại từ vựng tiếp theo.',
    PRIMARY KEY(`progress_id`),
    FOREIGN KEY (`user_id`) REFERENCES `User`(`user_id`) ON UPDATE NO ACTION ON DELETE CASCADE,
    FOREIGN KEY (`vocab_id`) REFERENCES `Vocabulary`(`vocab_id`) ON UPDATE NO ACTION ON DELETE CASCADE
);

CREATE TABLE `Subscription` (
    `subscription_id` INTEGER NOT NULL AUTO_INCREMENT UNIQUE COMMENT 'Khóa chính định danh bản ghi đăng ký.',
    `user_id` INTEGER COMMENT 'Khóa ngoại tham chiếu đến bảng User.',
    `plan` ENUM('6_months', '1_year', '3_years') COMMENT 'Loại gói dịch vụ.',
    `start_date` DATETIME COMMENT 'Ngày bắt đầu gói dịch vụ.',
    `end_date` DATETIME COMMENT 'Ngày kết thúc gói dịch vụ.',
    PRIMARY KEY(`subscription_id`),
    FOREIGN KEY (`user_id`) REFERENCES `User`(`user_id`) ON UPDATE NO ACTION ON DELETE CASCADE
);

CREATE TABLE `Payment` (
    `payment_id` INTEGER NOT NULL AUTO_INCREMENT UNIQUE COMMENT 'Khóa chính định danh giao dịch thanh toán.',
    `user_id` INTEGER COMMENT 'Khóa ngoại tham chiếu đến bảng User.',
    `amount` DECIMAL(10, 2) COMMENT 'Số tiền thanh toán.',
    `payment_date` DATETIME COMMENT 'Ngày thanh toán.',
    `payment_method` VARCHAR(255) COMMENT 'Phương thức thanh toán.',
    PRIMARY KEY(`payment_id`),
    FOREIGN KEY (`user_id`) REFERENCES `User`(`user_id`) ON UPDATE NO ACTION ON DELETE CASCADE
);
