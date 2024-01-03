CREATE TABLE persons (
                         id bigserial constraint PK_persons PRIMARY KEY,
                         first_name varchar(255) not null,
                         last_name varchar(255) not null,
                         age int not null
)