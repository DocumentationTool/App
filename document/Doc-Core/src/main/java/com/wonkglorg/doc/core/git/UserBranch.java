package com.wonkglorg.doc.core.git;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.MergeResult;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.lib.Ref;
import org.eclipse.jgit.transport.UsernamePasswordCredentialsProvider;

import java.nio.file.Path;

/**
 * Represents a branch for a user in a git repo
 */
public class UserBranch {
	private final GitRepo repo;
	private final String userId;
	private Ref branch;
	
	public UserBranch(GitRepo repo, String userId) throws GitAPIException {
		this.repo = repo;
		this.userId = userId;
		this.branch = repo.getGit().getRepository().findRef(userId);
		
		// Create the branch if it does not exist
		if (this.branch == null) {
			createBranch();
		}
	}
	
	public void addFile(Path file) throws GitAPIException {
		Git git = repo.getGit();
		String repoRelativePath = git.getRepository().getWorkTree().toPath().relativize(file).toString();
		
		git.checkout().setName(userId).call();
		git.add().addFilepattern(repoRelativePath).call();
	}
	
	public void commit(String message) throws GitAPIException {
		Git git = repo.getGit();
		git.checkout().setName(userId).call();
		
		git.commit()
		   .setMessage(message)
		   .setAuthor(userId, "email@example.com")
		   .call();
	}
	
	public void push(String username, String password) throws GitAPIException {
		Git git = repo.getGit();
		git.checkout().setName(userId).call();
		
		git.push()
		   .setRemote("origin")
		   .setCredentialsProvider(new UsernamePasswordCredentialsProvider(username, password))
		   .call();
	}
	
	public void closeBranch() throws GitAPIException {
		Git git = repo.getGit();
		git.checkout().setName("main").call();  // Switch to another branch before deleting
		git.branchDelete().setBranchNames(userId).setForce(true).call();
	}
	
	public void createBranch() throws GitAPIException {
		Git git = repo.getGit();
		this.branch = git.branchCreate().setName(userId).call();
	}
	
	public void mergeIntoMain() throws GitAPIException {
		Git git = repo.getGit();
		
		// Ensure the branch exists before merging
		if (git.getRepository().findRef(userId) == null) {
			throw new GitAPIException("Branch does not exist: " + userId) {};
		}
		
		// Switch to main branch before merging
		git.checkout().setName("main").call();
		
		// Merge user branch into main
		MergeResult mergeResult = git.merge()
									 .include(git.getRepository().findRef(userId))
									 .call();
		
		// Check merge result
		if (!mergeResult.getMergeStatus().isSuccessful()) {
			throw new GitAPIException("Merge conflict occurred: " + mergeResult.getMergeStatus()) {};
		}
	}
}
