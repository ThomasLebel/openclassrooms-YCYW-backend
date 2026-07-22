CREATE TABLE `USERS` (
  `id` integer PRIMARY KEY AUTO_INCREMENT,
  `role` varchar(50),
  `email` varchar(255) NOT NULL,
  `password` varchar(255) NOT NULL,
  `first_name` varchar(255),
  `last_name` varchar(255),
  `birth_date` date,
  `language` varchar(10),
  `currency` varchar(3),
  `created_at` timestamp,
  `updated_at` timestamp
);

CREATE TABLE `ADDRESSES` (
  `id` integer PRIMARY KEY AUTO_INCREMENT,
  `user_id` integer NOT NULL,
  `street` varchar(255),
  `city` varchar(255),
  `zip_code` varchar(20),
  `country` varchar(255),
  `is_default` boolean DEFAULT false
);


CREATE TABLE `AGENCIES` (
  `id` integer PRIMARY KEY AUTO_INCREMENT,
  `name` varchar(255),
  `street` varchar(255),
  `city` varchar(255),
  `zip_code` varchar(20),
  `country` varchar(255),
  `phone` varchar(30)
);

CREATE TABLE `VEHICULE_CATEGORIES` (
  `id` integer PRIMARY KEY AUTO_INCREMENT,
  `acriss_code` varchar(10) NOT NULL,
  `name` varchar(255),
  `description` varchar(2000)
);

CREATE TABLE `OFFERS` (
  `id` integer PRIMARY KEY AUTO_INCREMENT,
  `departure_agency_id` integer NOT NULL,
  `vehicule_category_id` integer NOT NULL,
  `price_per_day` numeric,
  `currency` varchar(3)
);

CREATE TABLE `BOOKINGS` (
  `id` integer PRIMARY KEY AUTO_INCREMENT,
  `user_id` integer NOT NULL,
  `offer_id` integer NOT NULL,
  `return_agency_id` integer NOT NULL,
  `departure_at` timestamp,
  `return_at` timestamp,
  `total_price` numeric,
  `currency` varchar(3),
  `status` varchar(50),
  `booked_at` timestamp,
  `cancelled_at` timestamp
);

CREATE TABLE `PAYMENTS` (
  `id` integer PRIMARY KEY AUTO_INCREMENT,
  `booking_id` integer NOT NULL,
  `stripe_id` varchar(255),
  `amount` numeric,
  `currency` varchar(3),
  `status` varchar(50),
  `paid_at` timestamp
);


CREATE TABLE `SUPPORT_TICKETS` (
  `id` integer PRIMARY KEY AUTO_INCREMENT,
  `user_id` integer NOT NULL,
  `agent_id` integer,
  `subject` varchar(255),
  `status` varchar(50),
  `created_at` timestamp,
  `updated_at` timestamp
);

CREATE TABLE `MESSAGES` (
  `id` integer PRIMARY KEY AUTO_INCREMENT,
  `ticket_id` integer NOT NULL,
  `content` varchar(2000),
  `sender_pseudo` varchar(255),
  `sent_at` timestamp,
  `read_at` timestamp
);

CREATE UNIQUE INDEX `USERS_email_index` ON `USERS` (`email`);
CREATE UNIQUE INDEX `VEHICULE_CATEGORIES_acriss_code_index` ON `VEHICULE_CATEGORIES` (`acriss_code`);
CREATE UNIQUE INDEX `PAYMENTS_booking_id_index` ON `PAYMENTS` (`booking_id`);


ALTER TABLE `ADDRESSES` ADD FOREIGN KEY (`user_id`) REFERENCES `USERS` (`id`);

ALTER TABLE `OFFERS` ADD FOREIGN KEY (`departure_agency_id`) REFERENCES `AGENCIES` (`id`);

ALTER TABLE `OFFERS` ADD FOREIGN KEY (`vehicule_category_id`) REFERENCES `VEHICULE_CATEGORIES` (`id`);

ALTER TABLE `BOOKINGS` ADD FOREIGN KEY (`user_id`) REFERENCES `USERS` (`id`);

ALTER TABLE `BOOKINGS` ADD FOREIGN KEY (`offer_id`) REFERENCES `OFFERS` (`id`);

ALTER TABLE `BOOKINGS` ADD FOREIGN KEY (`return_agency_id`) REFERENCES `AGENCIES` (`id`);

ALTER TABLE `PAYMENTS` ADD FOREIGN KEY (`booking_id`) REFERENCES `BOOKINGS` (`id`);

ALTER TABLE `SUPPORT_TICKETS` ADD FOREIGN KEY (`user_id`) REFERENCES `USERS` (`id`);

ALTER TABLE `SUPPORT_TICKETS` ADD FOREIGN KEY (`agent_id`) REFERENCES `USERS` (`id`);

ALTER TABLE `MESSAGES` ADD FOREIGN KEY (`ticket_id`) REFERENCES `SUPPORT_TICKETS` (`id`);