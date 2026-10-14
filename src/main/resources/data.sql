INSERT INTO autor (nome, nacionalidade) VALUES ('Machado de Assis', 'Brasileiro');
INSERT INTO autor (nome, nacionalidade) VALUES ('Clarice Lispector', 'Brasileira');
INSERT INTO autor (nome, nacionalidade) VALUES ('George Orwell', 'Britânico');

INSERT INTO livro (titulo, isbn, ano_publicacao, autor_id) VALUES ('Dom Casmurro', '978-85-01-00100-1', 1899, (SELECT id FROM autor WHERE nome = 'Machado de Assis'));
INSERT INTO livro (titulo, isbn, ano_publicacao, autor_id) VALUES ('Memórias Póstumas de Brás Cubas', '978-85-01-00101-8', 1881, (SELECT id FROM autor WHERE nome = 'Machado de Assis'));
INSERT INTO livro (titulo, isbn, ano_publicacao, autor_id) VALUES ('A Hora da Estrela', '978-85-01-00102-5', 1977, (SELECT id FROM autor WHERE nome = 'Clarice Lispector'));
INSERT INTO livro (titulo, isbn, ano_publicacao, autor_id) VALUES ('1984', '978-85-01-00103-2', 1949, (SELECT id FROM autor WHERE nome = 'George Orwell'));
INSERT INTO livro (titulo, isbn, ano_publicacao, autor_id) VALUES ('A Revolução dos Bichos', '978-85-01-00104-9', 1945, (SELECT id FROM autor WHERE nome = 'George Orwell'));
