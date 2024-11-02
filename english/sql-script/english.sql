CREATE DATABASE IF NOT EXISTS `english`;
USE `english`;

CREATE TABLE `User` (
    `user_id` INTEGER NOT NULL AUTO_INCREMENT UNIQUE COMMENT 'Khóa chính để xác định duy nhất mỗi người dùng.',
    `email` VARCHAR(255) NOT NULL UNIQUE COMMENT 'Địa chỉ email của người dùng, thường dùng để đăng nhập.',
    `password` VARCHAR(255) NOT NULL COMMENT 'Mật khẩu đã được mã hóa để bảo mật.',
    `full_name` VARCHAR(255) NOT NULL,
    `enable` TINYINT(1) DEFAULT 1,
    `subscription_plan` ENUM('none', '6_months', '1_year', '3_years') DEFAULT 'none' COMMENT 'Gói dịch vụ mà người dùng đã đăng ký.',
    `subscription_start_date` DATETIME COMMENT 'Ngày bắt đầu gói dịch vụ.',
    `subscription_end_date` DATETIME COMMENT 'Ngày kết thúc gói dịch vụ, dùng để xác định khi nào người dùng cần đăng ký lại.',
    `role` ENUM('ROLE_user', 'ROLE_admin') NOT NULL DEFAULT 'ROLE_user' COMMENT 'Xác định vai trò của người dùng.',
    `paid` TINYINT(1) DEFAULT 0,
    `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT 'Thời gian tạo người dùng.',
    `updated_at` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT 'Thời gian cập nhật thông tin người dùng.',
    PRIMARY KEY(`user_id`)
);

CREATE TABLE `Course` (
    `course_id` INTEGER NOT NULL AUTO_INCREMENT UNIQUE COMMENT 'Khóa chính định danh khóa học.',
    `course_name` VARCHAR(255) NOT NULL COMMENT 'Tên khóa học.',
    `description` TEXT NOT NULL COMMENT 'Mô tả chi tiết về khóa học.',
    `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT 'Thời gian tạo bản ghi.',
    `updated_at` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT 'Thời gian cập nhật bản ghi.',
    PRIMARY KEY(`course_id`)
);

CREATE TABLE `Topic` (
    `topic_id` INTEGER NOT NULL AUTO_INCREMENT UNIQUE COMMENT 'Khóa chính định danh chủ đề.',
    `topic_name` VARCHAR(255) COMMENT 'Tên của chủ đề (ví dụ: Nghề nghiệp, Tính cách).',
    `course_id` INTEGER COMMENT 'Khóa ngoại tham chiếu đến bảng Course.',
    `description` TEXT COMMENT 'Mô tả chi tiết về chủ đề.',
    `order` INTEGER COMMENT 'Xác định thứ tự của chủ đề trong khóa học.',
    `image` VARCHAR(255) COMMENT 'Đường dẫn đến ảnh đại diện của chủ đề.',
    `content` TEXT COMMENT 'Nội dung chi tiết về chủ đề.',
    `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT 'Thời gian tạo bản ghi.',
    `updated_at` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT 'Thời gian cập nhật bản ghi.',
    PRIMARY KEY(`topic_id`),
    FOREIGN KEY (`course_id`) REFERENCES `Course`(`course_id`) ON UPDATE CASCADE ON DELETE SET NULL
);

CREATE TABLE `Vocabulary` (
    `vocab_id` INTEGER NOT NULL AUTO_INCREMENT UNIQUE COMMENT 'Khóa chính định danh từ vựng.',
    `word` VARCHAR(255) COMMENT 'Từ vựng (ví dụ: "doctor", "teacher").',
    `meaning` TEXT COMMENT 'Định nghĩa của từ.',
    `example_sentence` TEXT COMMENT 'Ví dụ về câu chứa từ.',
    `topic_id` INTEGER COMMENT 'Khóa ngoại tham chiếu đến bảng Topic.', 
    `pronunciation` TEXT COMMENT 'Phát âm.',
    `image` VARCHAR(255) COMMENT 'Đường dẫn đến ảnh đại diện của chủ đề.',
    `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT 'Thời gian tạo bản ghi.',
    `updated_at` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT 'Thời gian cập nhật bản ghi.',
    PRIMARY KEY(`vocab_id`),
    FOREIGN KEY (`topic_id`) REFERENCES `Topic`(`topic_id`) ON UPDATE CASCADE ON DELETE SET NULL
);

CREATE TABLE `User_Progress` (
    `progress_id` INTEGER NOT NULL AUTO_INCREMENT UNIQUE COMMENT 'Khóa chính định danh mỗi bản ghi tiến trình.',
    `user_id` INTEGER NOT NULL COMMENT 'Khóa ngoại tham chiếu đến bảng User.',
    `vocab_id` INTEGER NOT NULL COMMENT 'Khóa ngoại tham chiếu đến bảng Vocabulary.',
    `review_interval` INTEGER COMMENT 'Khoảng thời gian lặp lại ngắt quãng.',
    `last_reviewed` DATETIME COMMENT 'Thời điểm lần ôn tập gần nhất.',
    `next_review` DATETIME COMMENT 'Thời điểm người dùng cần ôn lại từ vựng tiếp theo.',
    PRIMARY KEY(`progress_id`),
    FOREIGN KEY (`user_id`) REFERENCES `User`(`user_id`) ON UPDATE CASCADE ON DELETE CASCADE,
    FOREIGN KEY (`vocab_id`) REFERENCES `Vocabulary`(`vocab_id`) ON UPDATE CASCADE ON DELETE CASCADE
);

CREATE TABLE `Subscription` (
    `subscription_id` INTEGER NOT NULL AUTO_INCREMENT UNIQUE COMMENT 'Khóa chính định danh bản ghi đăng ký.',
    `user_id` INTEGER NOT NULL COMMENT 'Khóa ngoại tham chiếu đến bảng User.',
    `plan` ENUM('6_months', '1_year', '3_years') NOT NULL COMMENT 'Loại gói dịch vụ.',
    `start_date` DATETIME COMMENT 'Ngày bắt đầu gói dịch vụ.',
    `end_date` DATETIME COMMENT 'Ngày kết thúc gói dịch vụ.',
    PRIMARY KEY(`subscription_id`),
    FOREIGN KEY (`user_id`) REFERENCES `User`(`user_id`) ON UPDATE CASCADE ON DELETE CASCADE
);

CREATE TABLE `Folder` (
    `folder_id` INTEGER NOT NULL AUTO_INCREMENT UNIQUE COMMENT 'Khóa chính định danh thư mục.',
    `user_id` INTEGER NOT NULL COMMENT 'Khóa ngoại tham chiếu đến bảng User.',
    `folder_name` VARCHAR(255) NOT NULL COMMENT 'Tên thư mục.',
    `description` TEXT COMMENT 'Mô tả chi tiết về thư mục.',
    PRIMARY KEY(`folder_id`),
    FOREIGN KEY (`user_id`) REFERENCES `User`(`user_id`) ON UPDATE CASCADE ON DELETE CASCADE
);

CREATE TABLE `Folder_Vocabulary` (
    `folder_id` INTEGER NOT NULL COMMENT 'Khóa ngoại tham chiếu đến bảng Folder.',
    `vocab_id` INTEGER NOT NULL COMMENT 'Khóa ngoại tham chiếu đến bảng Vocabulary.',
    PRIMARY KEY(`folder_id`, `vocab_id`),
    FOREIGN KEY (`folder_id`) REFERENCES `Folder`(`folder_id`) ON UPDATE CASCADE ON DELETE CASCADE,
    FOREIGN KEY (`vocab_id`) REFERENCES `Vocabulary`(`vocab_id`) ON UPDATE CASCADE ON DELETE CASCADE
);

CREATE TABLE `Form_Submission` (
    `submission_id` INTEGER NOT NULL AUTO_INCREMENT UNIQUE COMMENT 'Khóa chính định danh mỗi lần gửi biểu mẫu.',
    `user_id` INTEGER NOT NULL COMMENT 'Khóa ngoại tham chiếu đến bảng User.',
    `form_type` TINYINT(1) NOT NULL COMMENT 'Loại biểu mẫu: 0 = đánh giá, 1 = gửi lỗi.',
    `content` TEXT NOT NULL COMMENT 'Nội dung của biểu mẫu.',
    `status` TINYINT(1) DEFAULT 0 COMMENT 'Trạng thái xử lý của biểu mẫu: 0 = chưa xử lý, 1 = đã xử lý.',
    PRIMARY KEY(`submission_id`),
    FOREIGN KEY (`user_id`) REFERENCES `User`(`user_id`) ON UPDATE CASCADE ON DELETE CASCADE
);
