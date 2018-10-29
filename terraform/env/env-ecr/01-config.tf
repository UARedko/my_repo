terraform {
  backend "consul" {
    address    = "127.0.0.1:8500"
    datacenter = "us-east-1-tonic-dev-mgmt"
    path       = "terraform/tonic/ecr/state"
  }
}

provider "aws" {
  region = "${var.region}"
}

provider "consul" {
  address    = "${var.consul_address}"
  datacenter = "${var.consul_datacenter}"
}