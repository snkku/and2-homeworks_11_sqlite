package ru.netology.nmedia.dao

import android.content.ContentValues
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import ru.netology.nmedia.dto.Post

class PostDaoImpl(private val db: SQLiteDatabase) : PostDao {
    companion object {
        val DDL = """
        CREATE TABLE ${PostColumns.TABLE} (
            ${PostColumns.COLUMN_ID} INTEGER PRIMARY KEY AUTOINCREMENT,
            ${PostColumns.COLUMN_AUTHOR} TEXT NOT NULL,
            ${PostColumns.COLUMN_CONTENT} TEXT NOT NULL,
            ${PostColumns.COLUMN_PUBLISHED} TEXT NOT NULL DEFAULT CURRENT_TIMESTAMP,
            ${PostColumns.COLUMN_LIKED_BY_ME} BOOLEAN NOT NULL DEFAULT 0,
            ${PostColumns.COLUMN_LIKES} INTEGER NOT NULL DEFAULT 0,
            ${PostColumns.COLUMN_SHARED} INTEGER NOT NULL DEFAULT 0,
            ${PostColumns.COLUMN_VIEWED} INTEGER NOT NULL DEFAULT 1,
            ${PostColumns.COLUMN_VIDEO} TEXT DEFAULT NULL
            
        );
        """.trimIndent()
    }

    object PostColumns {
        const val TABLE = "posts"
        const val COLUMN_ID = "id"
        const val COLUMN_AUTHOR = "author"
        const val COLUMN_CONTENT = "content"
        const val COLUMN_PUBLISHED = "published"
        const val COLUMN_LIKED_BY_ME = "likedByMe"
        const val COLUMN_LIKES = "likes"
        const val COLUMN_SHARED = "shared"
        const val COLUMN_VIEWED = "viewed"
        const val COLUMN_VIDEO = "videoURL"
        val ALL_COLUMNS = arrayOf(
            COLUMN_ID,
            COLUMN_AUTHOR,
            COLUMN_CONTENT,
            COLUMN_PUBLISHED,
            COLUMN_LIKED_BY_ME,
            COLUMN_LIKES,
            COLUMN_SHARED,
            COLUMN_VIDEO,
            COLUMN_VIEWED
        )
    }

    override fun getAll(): List<Post> {
        val posts = mutableListOf<Post>()
        db.query(
            PostColumns.TABLE,
            PostColumns.ALL_COLUMNS,
            null,
            null,
            null,
            null,
            "${PostColumns.COLUMN_ID} DESC"
        ).use {
            while (it.moveToNext()) {
                posts.add(map(it))
            }
        }
        return posts
    }

    override fun save(post: Post): Post {
        val values = ContentValues().apply {
            // TODO: remove hardcoded values
            put(PostColumns.COLUMN_AUTHOR, "Me")
            put(PostColumns.COLUMN_CONTENT, post.content)
            put(PostColumns.COLUMN_VIDEO, post.videoURL)
        }
        val id = if (post.id != 0L) {
            db.update(
                PostColumns.TABLE,
                values,
                "${PostColumns.COLUMN_ID} = ?",
                arrayOf(post.id.toString()),
            )
            post.id
        } else {
            db.insert(PostColumns.TABLE, null, values)
        }
        db.query(
            PostColumns.TABLE,
            PostColumns.ALL_COLUMNS,
            "${PostColumns.COLUMN_ID} = ?",
            arrayOf(id.toString()),
            null,
            null,
            null,
        ).use {
            it.moveToNext()
            return map(it)
        }
    }

    override fun likeById(id: Long) {
        db.execSQL(
            """
           UPDATE ${PostColumns.TABLE} SET
               ${PostColumns.COLUMN_LIKES} = ${PostColumns.COLUMN_LIKES} + CASE WHEN ${PostColumns.COLUMN_LIKED_BY_ME} THEN -1 ELSE 1 END,
               ${PostColumns.COLUMN_LIKED_BY_ME} = CASE WHEN ${PostColumns.COLUMN_LIKED_BY_ME} THEN 0 ELSE 1 END
           WHERE ${PostColumns.COLUMN_ID} = ?;
        """.trimIndent(), arrayOf(id.toString())
        )
    }

    override fun share(id: Long) {
        db.execSQL(
            """
                UPDATE ${PostColumns.TABLE} SET
                    ${PostColumns.COLUMN_SHARED} = ${PostColumns.COLUMN_SHARED} + 1
                WHERE ${PostColumns.COLUMN_ID} = ?
            """.trimIndent(), arrayOf(id.toString())
        )
    }

    override fun view(id: Long) {
        db.execSQL(
            """
                UPDATE ${PostColumns.TABLE} SET
                    ${PostColumns.COLUMN_VIEWED} = ${PostColumns.COLUMN_VIEWED} + 1
                WHERE ${PostColumns.COLUMN_ID} = ?
            """.trimIndent(), arrayOf(id.toString())
        )
    }

    override fun removeById(id: Long) {
        db.delete(
            PostColumns.TABLE,
            "${PostColumns.COLUMN_ID} = ?",
            arrayOf(id.toString())
        )
    }

    private fun map(cursor: Cursor): Post {
        with(cursor) {
            return Post(
                id = getLong(getColumnIndexOrThrow(PostColumns.COLUMN_ID)),
                author = getString(getColumnIndexOrThrow(PostColumns.COLUMN_AUTHOR)),
                content = getString(getColumnIndexOrThrow(PostColumns.COLUMN_CONTENT)),
                published = getString(getColumnIndexOrThrow(PostColumns.COLUMN_PUBLISHED)),
                likedByMe = getInt(getColumnIndexOrThrow(PostColumns.COLUMN_LIKED_BY_ME)) != 0,
                likes = getInt(getColumnIndexOrThrow(PostColumns.COLUMN_LIKES)),
                viewed = getInt(getColumnIndexOrThrow(PostColumns.COLUMN_VIEWED)),
                shared = getInt(getColumnIndexOrThrow(PostColumns.COLUMN_SHARED)),
                videoURL = getString(getColumnIndexOrThrow(PostColumns.COLUMN_VIDEO))
            )
        }
    }
}