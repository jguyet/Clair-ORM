/*
Navicat MySQL Data Transfer

Source Server         : localhost
Source Server Version : 50617
Source Host           : localhost:3306
Source Database       : test

Target Server Type    : MYSQL
Target Server Version : 50617
File Encoding         : 65001

Date: 2018-07-18 21:57:33
*/

SET FOREIGN_KEY_CHECKS=0;

-- ----------------------------
-- Table structure for `accounts`
-- ----------------------------
DROP TABLE IF EXISTS `accounts`;
CREATE TABLE `accounts` (
  `id` varchar(25) NOT NULL,
  `account` varchar(30) CHARACTER SET latin1 NOT NULL,
  `password` text CHARACTER SET latin1 NOT NULL,
  `right` int(11) DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `account` (`account`) USING BTREE
) ENGINE=MyISAM DEFAULT CHARSET=utf8;

-- ----------------------------
-- Records of accounts
-- ----------------------------
INSERT INTO `accounts` VALUES ('1', 'fdherh', 'ehrheh', null);
INSERT INTO `accounts` VALUES ('2', 'jjjjj', 'erhreh', null);
