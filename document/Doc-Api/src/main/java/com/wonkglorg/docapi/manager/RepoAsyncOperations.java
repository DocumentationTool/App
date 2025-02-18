package com.wonkglorg.docapi.manager;

import com.wonkglorg.docapi.db.objects.Resource;
import com.wonkglorg.docapi.manager.RepoManager.FileRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

@Component
public class RepoAsyncOperations{
	
	
	
	@Async
	public CompletableFuture<Void> initializeRepositoryAsync(FileRepository repository) {
		return CompletableFuture.runAsync(() -> {
			try{
				log.info("Initializing Repo '{}'", repository.repoProperties.getName());
				repository.initialize();
			} catch(Exception e){
				log.error("Failed to initialize repository '{}'", repository.repoProperties.getName(), e);
			}
		});
	}
	
	// Perform search within a single repository asynchronously
	@Async
	public CompletableFuture<List<Resource>> searchInRepositoryAsync(FileRepository repository, String searchTerm) {
		return CompletableFuture.supplyAsync(() -> {
			try{
				log.info("Searching in Repo '{}' for term '{}'", repository.repoProperties.getName(), searchTerm);
				return repository.search(searchTerm); // Assuming 'search' is a method in FileRepository
			} catch(Exception e){
				log.error("Error searching in repository '{}'", repository.repoProperties.getName(), e);
				return new ArrayList<Resource>(); // Return empty list in case of error
			}
		});
	}
	
}
