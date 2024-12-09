# Casual Java Integration Test

The purpose of this repository is to perform integration testing on casual java.

This includes all casual java repositories e.g. `casual-java`, `casual-caller` etc

It is expected to run within a simple `k8s` compliant cluster using `tekton`.

The concept for integration tests is that is should perform tests that are not possible
from within unit tests, though should still not take a "long time" to run, ideally less 
than an hour for all integration tests.

All tests that are run here are expected to be run prior to/ in conjunction with a PR 
and a release.

The answer to the question of which tests have been run for a given release must always
be "All the tests.". Any other answer becomes too quickly too complicated.

The integration tests are limited to function tests and do not include non functional tests.

## Approach

The integration tests will be performed with a specific order, where the complexity gradually
increases through the test suites. This is to ensure that quicker tests will fail fast, reducing
duration of the feedback loop for developers running the integration tests.

These tests will be at different levels.

All integration tests are expected to be performed against a running instance of casual.

The initial setup will build or download the relevant artifacts to build a container image.

This container image will then become the container under test for the duration of the tests.

There may be test scenarios which require different configurations of the container to be performed.

These much be grouped together to ensure efficiency of the tests.

Readiness and Liveness probes must be configured carefully to ensure low latency startups of the environments
and reducing the delay prior to testing being "fired".

## Initial Setup

The integration tests shall run on a simple k8s cluster which has the following:
* Image Registry - for storing the build docker images.
* Tekton - installed for running integration test pipeline.

The [k8s-setup](k8s-setup) folder provides some examples for how this can be setup.