
Yes, correct! If yum is not available, it will throw a “command not found” error. Here’s how to handle it:

Step 1: Identify What Package Manager Is Available
Add this to your Dockerfile temporarily to check:

RUN which yum || which microdnf || which apt-get || which apk || echo "No package manager found"


Based on the result, use the right command:
If microdnf is available (common in micro/UBI images like yours):

RUN microdnf update libnghttp2 curl freetype && \
    microdnf clean all


If apt-get is available (Debian/Ubuntu based):

RUN apt-get update && \
    apt-get install -y --only-upgrade libnghttp2 curl freetype2 && \
    rm -rf /var/lib/apt/lists/*


If apk is available (Alpine based):

RUN apk update && \
    apk upgrade libnghttp2 curl freetype


Since your base image is a micro image
Your image tag is jre:17.0.18.0.8-micro-11023046 — the word micro strongly suggests it uses microdnf, not yum. So most likely this will work:

RUN microdnf update libnghttp2 curl freetype && \
    microdnf clean all


Try this first!​​​​​​​​​​​​​​​​