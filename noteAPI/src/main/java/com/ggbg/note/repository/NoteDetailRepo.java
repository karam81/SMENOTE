package com.ggbg.note.repository;

import java.util.List;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import com.ggbg.note.domain.entity.NoteDetailEntity;
import com.ggbg.note.domain.entity.NoteEntity;

public interface NoteDetailRepo extends MongoRepository<NoteEntity, Integer> {
	@Query(value = "{'_id': ?0}", fields = "{note: {'_id' : true, 'subject' : true}}")
	NoteEntity findNoteDetailList(int groupNo);

	@Query(value = "{'_id': ?0}", fields = "{note : {$elemMatch : {_id : ?1}}}")
	NoteEntity findNoteDetailContent(int groupNo, int noteNo);
}