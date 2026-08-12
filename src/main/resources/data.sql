MERGE INTO mpa_ratings (id, name, description) KEY(id) VALUES(1, 'G', 'Без возрастных ограничений');

MERGE INTO mpa_ratings (id, name, description) KEY(id) VALUES(2, 'PG', 'С родителями');

MERGE INTO mpa_ratings (id, name, description) KEY(id) VALUES(3, 'PG-13', 'От 13 лет');

MERGE INTO mpa_ratings (id, name, description) KEY(id) VALUES(4, 'R', 'От 17 лет с родителем');

MERGE INTO mpa_ratings (id, name, description) KEY(id) VALUES(5, 'NC-17', 'Только от 18');


MERGE INTO genres (id, name) KEY(id) VALUES(1, 'Комедия');

MERGE INTO genres (id, name) KEY(id) VALUES(2, 'Драма');

MERGE INTO genres (id, name) KEY(id) VALUES(3, 'Мультфильм');

MERGE INTO genres (id, name) KEY(id) VALUES(4, 'Триллер');

MERGE INTO genres (id, name) KEY(id) VALUES(5, 'Документальный');

MERGE INTO genres (id, name) KEY(id) VALUES(6, 'Боевик');
