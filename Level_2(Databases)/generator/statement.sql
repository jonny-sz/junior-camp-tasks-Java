CREATE TABLE "article" (
    "article_id" SERIAL PRIMARY KEY,
    "article_title" VARCHAR(50),
    "article_text" TEXT,
    "article_created" TIMESTAMP(0) NOT NULL DEFAULT now(),
    "article_updated" TIMESTAMP(0) NOT NULL DEFAULT now()
);

CREATE TABLE "article__tag" (
    "article_id" INTEGER NOT NULL,
    "tag_id" INTEGER NOT NULL,
    PRIMARY KEY ("article_id", "tag_id")
);

CREATE TABLE "category" (
    "category_id" SERIAL PRIMARY KEY,
    "category_title" VARCHAR(50),
    "category_created" TIMESTAMP(0) NOT NULL DEFAULT now(),
    "category_updated" TIMESTAMP(0) NOT NULL DEFAULT now()
);

CREATE TABLE "tag" (
    "tag_id" SERIAL PRIMARY KEY,
    "tag_value" VARCHAR(50),
    "tag_created" TIMESTAMP(0) NOT NULL DEFAULT now(),
    "tag_updated" TIMESTAMP(0) NOT NULL DEFAULT now()
);

ALTER TABLE "article"
ADD "category_id" INTEGER NOT NULL,
ADD CONSTRAINT "fk_article_category_id"
FOREIGN KEY ("category_id")
REFERENCES "category" ("category_id");

ALTER TABLE "article__tag"
ADD CONSTRAINT "fk_article__tag_article_id"
FOREIGN KEY ("article_id")
REFERENCES "article" ("article_id");

ALTER TABLE "article__tag"
ADD CONSTRAINT "fk_article__tag_tag_id"
FOREIGN KEY ("tag_id")
REFERENCES "tag" ("tag_id");

CREATE FUNCTION update_article_timestamp()
RETURNS TRIGGER AS $$
    BEGIN
        NEW.article_updated = now();
        RETURN NEW;
    END;
$$ LANGUAGE 'plpgsql';
CREATE TRIGGER "tr_article_updated"
BEFORE UPDATE ON "article"
FOR EACH ROW
EXECUTE PROCEDURE update_article_timestamp();

CREATE FUNCTION update_category_timestamp()
RETURNS TRIGGER AS $$
    BEGIN
        NEW.category_updated = now();
        RETURN NEW;
    END;
$$ LANGUAGE 'plpgsql';
CREATE TRIGGER "tr_category_updated"
BEFORE UPDATE ON "category"
FOR EACH ROW
EXECUTE PROCEDURE update_category_timestamp();

CREATE FUNCTION update_tag_timestamp()
RETURNS TRIGGER AS $$
    BEGIN
        NEW.tag_updated = now();
        RETURN NEW;
    END;
$$ LANGUAGE 'plpgsql';
CREATE TRIGGER "tr_tag_updated"
BEFORE UPDATE ON "tag"
FOR EACH ROW
EXECUTE PROCEDURE update_tag_timestamp();

