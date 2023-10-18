FROM golang:alpine AS build

RUN apk add git

RUN mkdir /src
ADD . /src
WORKDIR /src

RUN go build -o ./out/go-sample-app .

FROM alpine:edge

COPY --from=build /src/out/go-sample-app /sbin/go-sample-app

CMD /sbin/go-sample-app
