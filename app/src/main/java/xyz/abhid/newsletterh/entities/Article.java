/*
 * Copyright (c) 2015, Abhishek Dabholkar
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice, this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the following disclaimer in the documentation and/or other materials provided with the distribution.
 *
 * 3. Neither the name of the copyright holder nor the names of its contributors may be used to endorse or promote products derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package xyz.abhid.newsletterh.entities;

import android.database.Cursor;

import com.google.gson.annotations.Expose;

import java.util.Date;

public class Article {

    @Expose
    public int id;

    @Expose
    public int issue;

    @Expose
    public Date issueDate;

    @Expose
    public String title;

    @Expose
    public String body;

    @Expose
    public String poster;

    @Expose
    public String author;

    @Expose
    public int authorYear;

    public boolean isFavorite;

    public boolean isRead;

    public static Article fromCursor(Cursor cursor) {
        Article article = new Article();

        article.id = cursor.getInt(0);
        article.issue = cursor.getInt(1);
        article.issueDate = new Date(cursor.getLong(2));
        article.title = cursor.getString(3);
        article.body = cursor.getString(4);
        article.poster = cursor.getString(5);
        article.author = cursor.getString(6);
        article.authorYear = cursor.getInt(7);
        article.isFavorite = cursor.getInt(8) == 1;
        article.isRead = cursor.getInt(9) == 1;
        return article;
    }

}
