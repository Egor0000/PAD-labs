#FROM golang:1.12-alpine AS build_base
#
#RUN apk add --no-cache git
#
## Set the Current Working Directory inside the container
#WORKDIR /tmp/go-sample-app
#
## We want to populate the module cache based on the go.{mod,sum} files.
#COPY go.mod .
#COPY go.sum .
#
##COPY main2.go .
#
#RUN go mod download
#
#COPY . .
#
## Unit tests
#RUN #CGO_ENABLED=0 go test -v
#
## Build the Go app
#RUN go build -o ./out/go-sample-app .
#
#
#EXPOSE 8005
#
## Run the binary program produced by `go install`
#CMD ["/tmp/go-sample-app/out/go-sample-app"]
#
### Start fresh from a smaller image
##FROM alpine:3.9
##RUN apk add ca-certificates
##
###COPY --from=build_base /tmp/go-sample-app/out/go-sample-app /app/go-sample-app
##
### This container exposes port 8080 to the outside world
##EXPOSE 8005
##
### Run the binary program produced by `go install`
##CMD ["/app/go-sample-app"]


FROM golang:alpine AS build

RUN apk add git

RUN mkdir /src
ADD . /src
WORKDIR /src

RUN go build -o ./out/go-sample-app .

FROM alpine:edge

COPY --from=build /src/out/go-sample-app /sbin/go-sample-app

CMD /sbin/go-sample-app
