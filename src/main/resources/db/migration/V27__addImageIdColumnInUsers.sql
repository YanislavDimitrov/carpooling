ALTER TABLE users
    Add COLUMN image_id INT,
    Add CONSTRAINT fk_users_images
        FOREIGN KEY (image_id)
            REFERENCES images(id)
