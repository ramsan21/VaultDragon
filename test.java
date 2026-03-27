FROM artifactory.global.standardchartered.com/gv-images-products/scb-bases/gts-10775/jre:17.0.18.0.8-micro-11023046

USER root

# Add this block to fix CVE-2026-27135 (libnghttp2) and other CVEs
RUN yum update -y libnghttp2 curl freetype && \
    yum clean all && \
    rm -rf /var/cache/yum

ARG UNAME=20001:20001
ARG BUILD_ARTIFACT
# ... rest remains the same
