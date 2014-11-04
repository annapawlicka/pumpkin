(ns pumpkin.dev.data
)

(def github-frequencies [{:week 1388275200, :value 434, :type :additions} {:week 1388275200, :value 0, :type :deletions} {:week 1388880000, :value 2477, :type :additions} {:week 1388880000, :value -1004, :type :deletions} {:week 1389484800, :value 52810, :type :additions} {:week 1389484800, :value -16279, :type :deletions} {:week 1390089600, :value 1802, :type :additions} {:week 1390089600, :value -1440, :type :deletions} {:week 1390694400, :value 547, :type :additions} {:week 1390694400, :value -271, :type :deletions} {:week 1391299200, :value 10766, :type :additions} {:week 1391299200, :value -203, :type :deletions} {:week 1391904000, :value 10962, :type :additions} {:week 1391904000, :value -1064, :type :deletions} {:week 1392508800, :value 3356, :type :additions} {:week 1392508800, :value -1814, :type :deletions} {:week 1393113600, :value 862, :type :additions} {:week 1393113600, :value -1124, :type :deletions} {:week 1393718400, :value 17820, :type :additions} {:week 1393718400, :value -569, :type :deletions} {:week 1394323200, :value 4174, :type :additions} {:week 1394323200, :value -17132, :type :deletions} {:week 1394928000, :value 552, :type :additions} {:week 1394928000, :value -418, :type :deletions} {:week 1395532800, :value 256, :type :additions} {:week 1395532800, :value -483, :type :deletions} {:week 1396137600, :value 1619, :type :additions} {:week 1396137600, :value -1444, :type :deletions} {:week 1396742400, :value 485, :type :additions} {:week 1396742400, :value -220, :type :deletions} {:week 1397347200, :value 14677, :type :additions} {:week 1397347200, :value -2254, :type :deletions} {:week 1397952000, :value 1254, :type :additions} {:week 1397952000, :value -13153, :type :deletions} {:week 1398556800, :value 1482, :type :additions} {:week 1398556800, :value -1486, :type :deletions} {:week 1399161600, :value 314, :type :additions} {:week 1399161600, :value -96, :type :deletions} {:week 1399766400, :value 45859, :type :additions} {:week 1399766400, :value -1244, :type :deletions} {:week 1400371200, :value 1854, :type :additions} {:week 1400371200, :value -1647, :type :deletions} {:week 1400976000, :value 29688, :type :additions} {:week 1400976000, :value -40765, :type :deletions} {:week 1401580800, :value 1693, :type :additions} {:week 1401580800, :value -1215, :type :deletions} {:week 1402185600, :value 21851, :type :additions} {:week 1402185600, :value -1712, :type :deletions} {:week 1402790400, :value 87903, :type :additions} {:week 1402790400, :value -64413, :type :deletions} {:week 1403395200, :value 4916, :type :additions} {:week 1403395200, :value -4313, :type :deletions} {:week 1404000000, :value 2633, :type :additions} {:week 1404000000, :value -956, :type :deletions} {:week 1404604800, :value 2653, :type :additions} {:week 1404604800, :value -1220, :type :deletions} {:week 1405209600, :value 2298, :type :additions} {:week 1405209600, :value -837, :type :deletions} {:week 1405814400, :value 854, :type :additions} {:week 1405814400, :value -874, :type :deletions} {:week 1406419200, :value 4775, :type :additions} {:week 1406419200, :value -22640, :type :deletions} {:week 1407024000, :value 2858, :type :additions} {:week 1407024000, :value -1063, :type :deletions} {:week 1407628800, :value 4177, :type :additions} {:week 1407628800, :value -2020, :type :deletions} {:week 1408233600, :value 5997, :type :additions} {:week 1408233600, :value -2444, :type :deletions} {:week 1408838400, :value 1424, :type :additions} {:week 1408838400, :value -1181, :type :deletions} {:week 1409443200, :value 3517, :type :additions} {:week 1409443200, :value -2898, :type :deletions} {:week 1410048000, :value 309, :type :additions} {:week 1410048000, :value -347, :type :deletions} {:week 1410652800, :value 845, :type :additions} {:week 1410652800, :value -582, :type :deletions} {:week 1411257600, :value 2010, :type :additions} {:week 1411257600, :value -1167, :type :deletions} {:week 1411862400, :value 758, :type :additions} {:week 1411862400, :value -2343, :type :deletions} {:week 1412467200, :value 996, :type :additions} {:week 1412467200, :value -554, :type :deletions} {:week 1413072000, :value 350, :type :additions} {:week 1413072000, :value -133, :type :deletions}])

(def first-week 1388275200)

(def contributors [{:total 62,
  :weeks
  [{:w 1388275200, :a 0, :d 0, :c 0}
   {:w 1388880000, :a 0, :d 0, :c 0}
   {:w 1389484800, :a 4, :d 2, :c 1}
   {:w 1390089600, :a 0, :d 0, :c 0}
   {:w 1390694400, :a 0, :d 0, :c 0}
   {:w 1391299200, :a 0, :d 0, :c 0}
   {:w 1391904000, :a 0, :d 0, :c 0}
   {:w 1392508800, :a 0, :d 0, :c 0}
   {:w 1393113600, :a 49, :d 39, :c 2}
   {:w 1393718400, :a 0, :d 0, :c 0}
   {:w 1394323200, :a 0, :d 0, :c 0}
   {:w 1394928000, :a 0, :d 0, :c 0}
   {:w 1395532800, :a 0, :d 0, :c 0}
   {:w 1396137600, :a 0, :d 0, :c 0}
   {:w 1396742400, :a 0, :d 0, :c 0}
   {:w 1397347200, :a 0, :d 0, :c 0}
   {:w 1397952000, :a 0, :d 0, :c 0}
   {:w 1398556800, :a 0, :d 0, :c 0}
   {:w 1399161600, :a 216, :d 19, :c 6}
   {:w 1399766400, :a 67, :d 54, :c 8}
   {:w 1400371200, :a 145, :d 59, :c 4}
   {:w 1400976000, :a 60, :d 12, :c 1}
   {:w 1401580800, :a 50, :d 23, :c 2}
   {:w 1402185600, :a 651, :d 119, :c 7}
   {:w 1402790400, :a 65, :d 42, :c 11}
   {:w 1403395200, :a 116, :d 54, :c 1}
   {:w 1404000000, :a 142, :d 95, :c 3}
   {:w 1404604800, :a 591, :d 59, :c 15}
   {:w 1405209600, :a 1, :d 0, :c 1}
   {:w 1405814400, :a 0, :d 0, :c 0}
   {:w 1406419200, :a 0, :d 0, :c 0}
   {:w 1407024000, :a 0, :d 0, :c 0}
   {:w 1407628800, :a 0, :d 0, :c 0}
   {:w 1408233600, :a 0, :d 0, :c 0}
   {:w 1408838400, :a 0, :d 0, :c 0}
   {:w 1409443200, :a 0, :d 0, :c 0}
   {:w 1410048000, :a 0, :d 0, :c 0}
   {:w 1410652800, :a 0, :d 0, :c 0}
   {:w 1411257600, :a 0, :d 0, :c 0}
   {:w 1411862400, :a 0, :d 0, :c 0}
   {:w 1412467200, :a 0, :d 0, :c 0}
   {:w 1413072000, :a 0, :d 0, :c 0}],
  :author
  {:html_url "https://github.com/bru",
   :gravatar_id "",
   :followers_url "https://api.github.com/users/bru/followers",
   :subscriptions_url "https://api.github.com/users/bru/subscriptions",
   :site_admin false,
   :following_url
   "https://api.github.com/users/bru/following{/other_user}",
   :type "User",
   :received_events_url
   "https://api.github.com/users/bru/received_events",
   :login "bru",
   :organizations_url "https://api.github.com/users/bru/orgs",
   :id 1563,
   :events_url "https://api.github.com/users/bru/events{/privacy}",
   :url "https://api.github.com/users/bru",
   :repos_url "https://api.github.com/users/bru/repos",
   :starred_url
   "https://api.github.com/users/bru/starred{/owner}{/repo}",
   :gists_url "https://api.github.com/users/bru/gists{/gist_id}",
   :avatar_url "https://avatars.githubusercontent.com/u/1563?v=2"}}
 {:total 170,
  :weeks
  [{:w 1388275200, :a 434, :d 0, :c 1}
   {:w 1388880000, :a 1970, :d 621, :c 34}
   {:w 1389484800, :a 51675, :d 15398, :c 50}
   {:w 1390089600, :a 970, :d 1009, :c 30}
   {:w 1390694400, :a 330, :d 186, :c 16}
   {:w 1391299200, :a 505, :d 187, :c 25}
   {:w 1391904000, :a 149, :d 0, :c 1}
   {:w 1392508800, :a 2523, :d 1506, :c 8}
   {:w 1393113600, :a 328, :d 248, :c 1}
   {:w 1393718400, :a 474, :d 104, :c 3}
   {:w 1394323200, :a 1400, :d 960, :c 1}
   {:w 1394928000, :a 0, :d 0, :c 0}
   {:w 1395532800, :a 0, :d 0, :c 0}
   {:w 1396137600, :a 0, :d 0, :c 0}
   {:w 1396742400, :a 0, :d 0, :c 0}
   {:w 1397347200, :a 0, :d 0, :c 0}
   {:w 1397952000, :a 0, :d 0, :c 0}
   {:w 1398556800, :a 0, :d 0, :c 0}
   {:w 1399161600, :a 0, :d 0, :c 0}
   {:w 1399766400, :a 0, :d 0, :c 0}
   {:w 1400371200, :a 0, :d 0, :c 0}
   {:w 1400976000, :a 0, :d 0, :c 0}
   {:w 1401580800, :a 0, :d 0, :c 0}
   {:w 1402185600, :a 0, :d 0, :c 0}
   {:w 1402790400, :a 0, :d 0, :c 0}
   {:w 1403395200, :a 0, :d 0, :c 0}
   {:w 1404000000, :a 0, :d 0, :c 0}
   {:w 1404604800, :a 0, :d 0, :c 0}
   {:w 1405209600, :a 0, :d 0, :c 0}
   {:w 1405814400, :a 0, :d 0, :c 0}
   {:w 1406419200, :a 0, :d 0, :c 0}
   {:w 1407024000, :a 0, :d 0, :c 0}
   {:w 1407628800, :a 0, :d 0, :c 0}
   {:w 1408233600, :a 0, :d 0, :c 0}
   {:w 1408838400, :a 0, :d 0, :c 0}
   {:w 1409443200, :a 0, :d 0, :c 0}
   {:w 1410048000, :a 0, :d 0, :c 0}
   {:w 1410652800, :a 0, :d 0, :c 0}
   {:w 1411257600, :a 0, :d 0, :c 0}
   {:w 1411862400, :a 0, :d 0, :c 0}
   {:w 1412467200, :a 0, :d 0, :c 0}
   {:w 1413072000, :a 0, :d 0, :c 0}],
  :author
  {:html_url "https://github.com/malcolmsparks",
   :gravatar_id "",
   :followers_url
   "https://api.github.com/users/malcolmsparks/followers",
   :subscriptions_url
   "https://api.github.com/users/malcolmsparks/subscriptions",
   :site_admin false,
   :following_url
   "https://api.github.com/users/malcolmsparks/following{/other_user}",
   :type "User",
   :received_events_url
   "https://api.github.com/users/malcolmsparks/received_events",
   :login "malcolmsparks",
   :organizations_url
   "https://api.github.com/users/malcolmsparks/orgs",
   :id 163131,
   :events_url
   "https://api.github.com/users/malcolmsparks/events{/privacy}",
   :url "https://api.github.com/users/malcolmsparks",
   :repos_url "https://api.github.com/users/malcolmsparks/repos",
   :starred_url
   "https://api.github.com/users/malcolmsparks/starred{/owner}{/repo}",
   :gists_url
   "https://api.github.com/users/malcolmsparks/gists{/gist_id}",
   :avatar_url "https://avatars.githubusercontent.com/u/163131?v=2"}}
 {:total 231,
  :weeks
  [{:w 1388275200, :a 0, :d 0, :c 0}
   {:w 1388880000, :a 16, :d 20, :c 3}
   {:w 1389484800, :a 519, :d 539, :c 10}
   {:w 1390089600, :a 0, :d 0, :c 0}
   {:w 1390694400, :a 0, :d 0, :c 0}
   {:w 1391299200, :a 10246, :d 0, :c 3}
   {:w 1391904000, :a 10813, :d 1064, :c 1}
   {:w 1392508800, :a 99, :d 65, :c 7}
   {:w 1393113600, :a 18, :d 3, :c 7}
   {:w 1393718400, :a 17255, :d 388, :c 3}
   {:w 1394323200, :a 1232, :d 294, :c 7}
   {:w 1394928000, :a 116, :d 76, :c 2}
   {:w 1395532800, :a 256, :d 483, :c 6}
   {:w 1396137600, :a 1, :d 1, :c 1}
   {:w 1396742400, :a 311, :d 114, :c 26}
   {:w 1397347200, :a 2040, :d 1820, :c 16}
   {:w 1397952000, :a 0, :d 0, :c 0}
   {:w 1398556800, :a 396, :d 161, :c 5}
   {:w 1399161600, :a 0, :d 0, :c 0}
   {:w 1399766400, :a 44163, :d 200, :c 7}
   {:w 1400371200, :a 1170, :d 1263, :c 14}
   {:w 1400976000, :a 0, :d 0, :c 0}
   {:w 1401580800, :a 19, :d 0, :c 1}
   {:w 1402185600, :a 0, :d 0, :c 0}
   {:w 1402790400, :a 0, :d 0, :c 0}
   {:w 1403395200, :a 479, :d 17, :c 1}
   {:w 1404000000, :a 805, :d 119, :c 2}
   {:w 1404604800, :a 777, :d 604, :c 6}
   {:w 1405209600, :a 794, :d 472, :c 23}
   {:w 1405814400, :a 78, :d 49, :c 8}
   {:w 1406419200, :a 261, :d 65, :c 2}
   {:w 1407024000, :a 692, :d 287, :c 12}
   {:w 1407628800, :a 0, :d 0, :c 0}
   {:w 1408233600, :a 374, :d 152, :c 8}
   {:w 1408838400, :a 0, :d 0, :c 0}
   {:w 1409443200, :a 2314, :d 2147, :c 26}
   {:w 1410048000, :a 130, :d 157, :c 4}
   {:w 1410652800, :a 250, :d 166, :c 13}
   {:w 1411257600, :a 205, :d 74, :c 5}
   {:w 1411862400, :a 0, :d 0, :c 0}
   {:w 1412467200, :a 0, :d 0, :c 0}
   {:w 1413072000, :a 4, :d 20, :c 2}],
  :author
  {:html_url "https://github.com/sw1nn",
   :gravatar_id "",
   :followers_url "https://api.github.com/users/sw1nn/followers",
   :subscriptions_url
   "https://api.github.com/users/sw1nn/subscriptions",
   :site_admin false,
   :following_url
   "https://api.github.com/users/sw1nn/following{/other_user}",
   :type "User",
   :received_events_url
   "https://api.github.com/users/sw1nn/received_events",
   :login "sw1nn",
   :organizations_url "https://api.github.com/users/sw1nn/orgs",
   :id 373335,
   :events_url "https://api.github.com/users/sw1nn/events{/privacy}",
   :url "https://api.github.com/users/sw1nn",
   :repos_url "https://api.github.com/users/sw1nn/repos",
   :starred_url
   "https://api.github.com/users/sw1nn/starred{/owner}{/repo}",
   :gists_url "https://api.github.com/users/sw1nn/gists{/gist_id}",
   :avatar_url "https://avatars.githubusercontent.com/u/373335?v=2"}}
 {:total 301,
  :weeks
  [{:w 1388275200, :a 0, :d 0, :c 0}
   {:w 1388880000, :a 491, :d 363, :c 9}
   {:w 1389484800, :a 612, :d 340, :c 13}
   {:w 1390089600, :a 832, :d 431, :c 13}
   {:w 1390694400, :a 217, :d 85, :c 5}
   {:w 1391299200, :a 15, :d 16, :c 3}
   {:w 1391904000, :a 0, :d 0, :c 0}
   {:w 1392508800, :a 734, :d 243, :c 8}
   {:w 1393113600, :a 467, :d 834, :c 3}
   {:w 1393718400, :a 91, :d 77, :c 2}
   {:w 1394323200, :a 1542, :d 15878, :c 14}
   {:w 1394928000, :a 436, :d 342, :c 3}
   {:w 1395532800, :a 0, :d 0, :c 0}
   {:w 1396137600, :a 1618, :d 1443, :c 12}
   {:w 1396742400, :a 174, :d 106, :c 15}
   {:w 1397347200, :a 12637, :d 434, :c 9}
   {:w 1397952000, :a 1230, :d 13120, :c 15}
   {:w 1398556800, :a 754, :d 878, :c 6}
   {:w 1399161600, :a 0, :d 0, :c 0}
   {:w 1399766400, :a 585, :d 256, :c 11}
   {:w 1400371200, :a 188, :d 139, :c 7}
   {:w 1400976000, :a 1275, :d 1391, :c 7}
   {:w 1401580800, :a 729, :d 511, :c 11}
   {:w 1402185600, :a 20768, :d 1000, :c 10}
   {:w 1402790400, :a 87564, :d 64172, :c 12}
   {:w 1403395200, :a 640, :d 725, :c 5}
   {:w 1404000000, :a 226, :d 71, :c 5}
   {:w 1404604800, :a 444, :d 72, :c 7}
   {:w 1405209600, :a 800, :d 299, :c 7}
   {:w 1405814400, :a 572, :d 751, :c 9}
   {:w 1406419200, :a 3369, :d 2710, :c 8}
   {:w 1407024000, :a 1710, :d 705, :c 8}
   {:w 1407628800, :a 1659, :d 449, :c 10}
   {:w 1408233600, :a 3028, :d 1323, :c 6}
   {:w 1408838400, :a 369, :d 187, :c 13}
   {:w 1409443200, :a 1063, :d 703, :c 7}
   {:w 1410048000, :a 0, :d 0, :c 0}
   {:w 1410652800, :a 524, :d 365, :c 5}
   {:w 1411257600, :a 715, :d 254, :c 8}
   {:w 1411862400, :a 278, :d 126, :c 5}
   {:w 1412467200, :a 885, :d 513, :c 10}
   {:w 1413072000, :a 0, :d 0, :c 0}],
  :author
  {:html_url "https://github.com/annapawlicka",
   :gravatar_id "",
   :followers_url
   "https://api.github.com/users/annapawlicka/followers",
   :subscriptions_url
   "https://api.github.com/users/annapawlicka/subscriptions",
   :site_admin false,
   :following_url
   "https://api.github.com/users/annapawlicka/following{/other_user}",
   :type "User",
   :received_events_url
   "https://api.github.com/users/annapawlicka/received_events",
   :login "annapawlicka",
   :organizations_url "https://api.github.com/users/annapawlicka/orgs",
   :id 2522010,
   :events_url
   "https://api.github.com/users/annapawlicka/events{/privacy}",
   :url "https://api.github.com/users/annapawlicka",
   :repos_url "https://api.github.com/users/annapawlicka/repos",
   :starred_url
   "https://api.github.com/users/annapawlicka/starred{/owner}{/repo}",
   :gists_url
   "https://api.github.com/users/annapawlicka/gists{/gist_id}",
   :avatar_url "https://avatars.githubusercontent.com/u/2522010?v=2"}}
 {:total 364,
  :weeks
  [{:w 1388275200, :a 0, :d 0, :c 0}
   {:w 1388880000, :a 0, :d 0, :c 0}
   {:w 1389484800, :a 0, :d 0, :c 0}
   {:w 1390089600, :a 0, :d 0, :c 0}
   {:w 1390694400, :a 0, :d 0, :c 0}
   {:w 1391299200, :a 0, :d 0, :c 0}
   {:w 1391904000, :a 0, :d 0, :c 0}
   {:w 1392508800, :a 0, :d 0, :c 0}
   {:w 1393113600, :a 0, :d 0, :c 0}
   {:w 1393718400, :a 0, :d 0, :c 0}
   {:w 1394323200, :a 0, :d 0, :c 0}
   {:w 1394928000, :a 0, :d 0, :c 0}
   {:w 1395532800, :a 0, :d 0, :c 0}
   {:w 1396137600, :a 0, :d 0, :c 0}
   {:w 1396742400, :a 0, :d 0, :c 0}
   {:w 1397347200, :a 0, :d 0, :c 0}
   {:w 1397952000, :a 24, :d 33, :c 2}
   {:w 1398556800, :a 332, :d 447, :c 11}
   {:w 1399161600, :a 98, :d 77, :c 4}
   {:w 1399766400, :a 1044, :d 734, :c 27}
   {:w 1400371200, :a 351, :d 186, :c 29}
   {:w 1400976000, :a 28353, :d 39362, :c 8}
   {:w 1401580800, :a 895, :d 681, :c 27}
   {:w 1402185600, :a 432, :d 593, :c 30}
   {:w 1402790400, :a 274, :d 199, :c 34}
   {:w 1403395200, :a 3681, :d 3517, :c 48}
   {:w 1404000000, :a 1460, :d 671, :c 22}
   {:w 1404604800, :a 841, :d 485, :c 22}
   {:w 1405209600, :a 703, :d 66, :c 12}
   {:w 1405814400, :a 204, :d 74, :c 5}
   {:w 1406419200, :a 726, :d 19381, :c 9}
   {:w 1407024000, :a 436, :d 71, :c 5}
   {:w 1407628800, :a 2194, :d 1551, :c 6}
   {:w 1408233600, :a 2588, :d 960, :c 18}
   {:w 1408838400, :a 137, :d 119, :c 3}
   {:w 1409443200, :a 140, :d 48, :c 7}
   {:w 1410048000, :a 179, :d 190, :c 3}
   {:w 1410652800, :a 71, :d 51, :c 12}
   {:w 1411257600, :a 1090, :d 839, :c 4}
   {:w 1411862400, :a 480, :d 2217, :c 5}
   {:w 1412467200, :a 111, :d 41, :c 7}
   {:w 1413072000, :a 346, :d 113, :c 4}],
  :author
  {:html_url "https://github.com/otfrom",
   :gravatar_id "",
   :followers_url "https://api.github.com/users/otfrom/followers",
   :subscriptions_url
   "https://api.github.com/users/otfrom/subscriptions",
   :site_admin false,
   :following_url
   "https://api.github.com/users/otfrom/following{/other_user}",
   :type "User",
   :received_events_url
   "https://api.github.com/users/otfrom/received_events",
   :login "otfrom",
   :organizations_url "https://api.github.com/users/otfrom/orgs",
   :id 64839,
   :events_url "https://api.github.com/users/otfrom/events{/privacy}",
   :url "https://api.github.com/users/otfrom",
   :repos_url "https://api.github.com/users/otfrom/repos",
   :starred_url
   "https://api.github.com/users/otfrom/starred{/owner}{/repo}",
   :gists_url "https://api.github.com/users/otfrom/gists{/gist_id}",
   :avatar_url "https://avatars.githubusercontent.com/u/64839?v=2"}}])
