package stanislaw.wojcik.githubapiconsumer.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import stanislaw.wojcik.githubapiconsumer.entity.Branch;
import stanislaw.wojcik.githubapiconsumer.entity.BranchDTO;
import stanislaw.wojcik.githubapiconsumer.entity.Repository;
import stanislaw.wojcik.githubapiconsumer.entity.RepositoryDTO;
import stanislaw.wojcik.githubapiconsumer.exception.UserNotFoundException;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
public class RepositoriesService {


    private final String branchUrl;
    private final String userUrl;

    private final WebClient webClient;

    public RepositoriesService(@Value("${base.url}") final String baseUrl,
                               @Value("${branch.url}") final String branchUrl,
                               @Value("${user.url}") final String userUrl) {
        this.branchUrl = branchUrl;
        this.userUrl = userUrl;
        this.webClient = WebClient.builder()
                .baseUrl(baseUrl)
                .build();
    }

    public List<Repository> getRepositories(final String username) {
        final var repositories = fetchRepositories(username);
        return Objects.requireNonNull(repositories).stream()
                .map(repo -> buildSingleRepository(username, repo))
                .collect(Collectors.toList());
    }

    private Repository buildSingleRepository(final String username, final RepositoryDTO repo) {
        return new Repository(repo.name(), repo.owner().login(), getBranchesForRepo(repo.name(), username));
    }

    private List<Branch> getBranchesForRepo(final String repo, final String username) {
        final var branches = fetchBranches(username, repo);
        return Objects.requireNonNull(branches).stream()
                .map(branch -> new Branch(branch.name(), branch.commit().sha()))
                .collect(Collectors.toList());
    }

    private List<BranchDTO> fetchBranches(final String username, final String repo) {
        return webClient.get()
                .uri(builder -> builder
                        .path(branchUrl)
                        .build(username, repo))
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<List<BranchDTO>>() {
                }).block();
    }

    private List<RepositoryDTO> fetchRepositories(final String username) {
        return webClient.get()
                .uri(builder -> builder
                        .path(userUrl)
                        .build(username))
                .exchangeToMono(response -> {
                    if (response.statusCode().equals(HttpStatus.OK)) {
                        return response.bodyToMono(new ParameterizedTypeReference<List<RepositoryDTO>>() {
                        });
                    } else {
                        throw new UserNotFoundException("User not found.");
                    }
                }).block();
    }
}
