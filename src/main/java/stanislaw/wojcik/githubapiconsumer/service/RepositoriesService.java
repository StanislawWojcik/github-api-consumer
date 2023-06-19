package stanislaw.wojcik.githubapiconsumer.service;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import stanislaw.wojcik.githubapiconsumer.entity.Branch;
import stanislaw.wojcik.githubapiconsumer.entity.BranchWrapper;
import stanislaw.wojcik.githubapiconsumer.entity.Repository;
import stanislaw.wojcik.githubapiconsumer.entity.RepositoryWrapper;
import stanislaw.wojcik.githubapiconsumer.exception.UserNotFoundException;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RepositoriesService {

    @Value("${user.url.prefix}")
    private String userUrlPrefix;

    @Value("${user.url.suffix}")
    private String userUrlSuffix;

    @Value("${repo.url.prefix}")
    private String repoUrlPrefix;

    @Value("${repo.url.suffix}")
    private String repoUrlSuffix;

    @Autowired
    private WebClient.Builder webClientBuilder;


    public List<Repository> getRepositories(final String username) {
        final var url = buildUrl(userUrlPrefix, username, userUrlSuffix);
        final var repositories = executeCall(url)
                .onStatus(HttpStatus.NOT_FOUND::equals,
                        clientResponse -> Mono.error(new UserNotFoundException("User not found.")))
                .bodyToMono(new ParameterizedTypeReference<List<RepositoryWrapper>>() {
                });
        return Objects.requireNonNull(repositories.block()).stream()
                .map(repo -> buildSingleRepository(username, repo))
                .collect(Collectors.toList());
    }

    private Repository buildSingleRepository(final String username, final RepositoryWrapper repo) {
        return new Repository(repo.name(), repo.owner().login(), getBranchesForRepo(repo.name(), username));
    }

    private List<Branch> getBranchesForRepo(final String repo, final String username) {
        final var branchUrl = buildUrl(repoUrlPrefix, username, repo, repoUrlSuffix);
        final var branches = executeCall(branchUrl)
                .bodyToMono(new ParameterizedTypeReference<List<BranchWrapper>>() {
                });
        return Objects.requireNonNull(branches.block()).stream()
                .map(branch -> new Branch(branch.name(), branch.commit().sha()))
                .collect(Collectors.toList());
    }

    private WebClient.ResponseSpec executeCall(final String url) {
        return webClientBuilder.build()
                .get()
                .uri(url)
                .retrieve();
    }

    private String buildUrl(final String... paths) {
        return String.join("/", paths);
    }
}
