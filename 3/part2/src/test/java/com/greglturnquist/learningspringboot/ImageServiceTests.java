/*
 * Copyright 2017 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.greglturnquist.learningspringboot;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.data.mongodb.repository.config.EnableReactiveMongoRepositories;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.util.FileCopyUtils;
import org.springframework.util.FileSystemUtils;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Hooks;
import reactor.core.publisher.Mono;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.Duration;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author Greg Turnquist
 */
@RunWith(SpringRunner.class)
@SpringBootTest(classes = ImageServiceTests.TestConfig.class)
@DataMongoTest
public class ImageServiceTests {

	@Autowired
	ImageRepository repository;

	@Autowired
	ImageService imageService;

	@Before
	public void setUp() throws IOException {
		repository
			.deleteAll()
			.log("setUp")
			.then(() -> Mono.when(
				repository.save(
					new Image("1",
						"learning-spring-boot-cover.jpg")).log("setUp"),
				repository.save(
					new Image("2",
						"learning-spring-boot-2nd-edition-cover.jpg")).log("setUp"),
				repository.save(
					new Image("3",
						"bazinga.png")).log("setUp")
				).log("setUp")
			)
			.log("setUp")
			.block(Duration.ofSeconds(30));

		FileSystemUtils.deleteRecursively(new File(ImageService.UPLOAD_ROOT));

		Files.createDirectory(Paths.get(ImageService.UPLOAD_ROOT));

		FileCopyUtils.copy("Test file",
			new FileWriter(ImageService.UPLOAD_ROOT +
				"/learning-spring-boot-cover.jpg"));

		FileCopyUtils.copy("Test file2",
			new FileWriter(ImageService.UPLOAD_ROOT +
				"/learning-spring-boot-2nd-edition-cover.jpg"));

		FileCopyUtils.copy("Test file3",
			new FileWriter(ImageService.UPLOAD_ROOT + "/bazinga.png"));

	}

	@Test
	public void findAllImages() {
		List<Image> images = imageService.findAllImages().collectList().block(Duration.ofSeconds(30));
		assertThat(images)
			.hasSize(3)
			.extracting("name").contains(
				"learning-spring-boot-cover.jpg",
				"bazinga.png",
				"learning-spring-boot-2nd-edition-cover.jpg");
	}

	@Test
	public void findOneImage() {
		Resource resource = imageService.findOneImage("learning-spring-boot-cover.jpg").block(Duration.ofSeconds(30));
		assertThat(resource.exists()).isTrue();
	}

	@Test
	public void deleteImage() {
		imageService.deleteImage("learning-spring-boot-cover.jpg").block(Duration.ofSeconds(30));
	}

	@Test
	public void createImages() {
		List<Image> images = imageService
			.createImage(Flux.just(
				new MockMultipartFile("alpha.png", "alpha.png", "PNG","data1".getBytes()),
				new MockMultipartFile("bravo.png", "bravo.png", "PNG","data2".getBytes())
			))
			.then(() -> imageService.findAllImages().collectList())
			.block(Duration.ofSeconds(30));

		assertThat(images).hasSize(5);
	}

	@Configuration
	@EnableReactiveMongoRepositories(basePackageClasses = ImageRepository.class)
	static class TestConfig {

		@Bean
		public ImageService imageService(ResourceLoader resourceLoader, ImageRepository imageRepository) {
			return new ImageService(resourceLoader, imageRepository);
		}

	}

}
