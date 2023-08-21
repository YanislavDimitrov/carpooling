CREATE TABLE tokens
(
    id      INT AUTO_INCREMENT PRIMARY KEY,
    token   VARCHAR(100) NOT NULL,
    user_id INT          NOT NULL,
    CONSTRAINT fk_tokens_users
        foreign key (user_id)
            REFERENCES users (id)
)