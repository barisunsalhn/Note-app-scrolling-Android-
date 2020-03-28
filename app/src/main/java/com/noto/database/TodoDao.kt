package com.noto.database

import androidx.lifecycle.LiveData
import androidx.room.*
import com.noto.todo.model.Todo

@Dao
interface TodoDao {
    @Query("SELECT * FROM todos WHERE todo_list_id= :todoListId")
    fun getTodos(todoListId: Long): LiveData<List<Todo>>

    @Query("SELECT * FROM todos WHERE todo_id = :todoId")
    fun getTodoById(todoId: Long): Todo

    @Insert
    fun insertTodo(todo: Todo)

    @Update
    fun updateTodo(todo: Todo)

    @Delete
    fun deleteTodo(todo: Todo)
}