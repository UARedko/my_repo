# Subnets
resource "aws_subnet" "appdb" {
  count                   = "${var.subnet_count}"
  vpc_id                  = "${var.vpc_id}"
  cidr_block              = "${cidrsubnet(var.vpc_cidr, var.subnet_cidr_newbits, (count.index * var.subnet_cidr_factor) + var.subnet_cidr_step )}"
  availability_zone       = "${element(split(",", lookup(var.azs, var.region)), count.index)}"
  map_public_ip_on_launch = false

  tags {
    Name        = "${var.owner}_${var.env}_${var.platform}_${var.type}_${var.subtype}"
    Owner       = "${var.owner}"
    Environment = "${var.env}"
    Platform    = "${var.platform}"
    Type        = "${var.type}"
    Subtype     = "${var.subtype}"
  }
}

resource "aws_route_table_association" "appdb" {
  count          = "${aws_subnet.appdb.count}"
  subnet_id      = "${element(aws_subnet.appdb.*.id, count.index)}"
  route_table_id = "${var.subnet_route_table}"
}
