

def writeI(i):
    size = len(i[0])
    with open(f"i{ size }.txt", 'w') as file:
        for c in i:
            for card in c:
                file.write(str(card) + " ")
            file.write('\n')

# 1s
i = []
for c1 in range(0, 16):
    i.append([c1])
writeI(i)
print(f"1 - { len(i) }")

# 2s
i = []
for c1 in range(0, 16):
    for c2 in range(c1 + 1, c1 + 6):
        if c2 > 15:
            continue

        c = [c1, c2]
        if c not in i:
            i.append(c)
i.append([0, 6])
writeI(i)
print(f"2 - { len(i) }")

# 3s
i = []
for c1 in range(0, 16):
    for c2 in range(c1 + 1, c1 + 6):
        for c3 in range(c2 + 1, c1 + 6):
            if c3 > 15:
                continue

            c = [c1, c2, c3]
            if c not in i:
                i.append(c)
i.append([0, 1, 6])
i.append([0, 2, 6])
i.append([0, 3, 6])
i.append([0, 4, 6])
i.append([0, 5, 6])
writeI(i)
print(f"3 - { len(i) }")

# 4s
i = []
for c1 in range(0, 16):
    for c2 in range(c1 + 1, c1 + 6):
        for c3 in range(c2 + 1, c1 + 6):
            for c4 in range(c3 + 1, c1 + 6):
                if c4 > 15:
                    continue

                c = [c1, c2, c3, c4]
                if c not in i:
                    i.append(c)
c1 = 0
c4 = 6
for c2 in range(1, 5):
    for c3 in range(c2 + 1, 6):
        c = [c1, c2, c3, c4]
        if c not in i:
            i.append(c)
writeI(i)
print(f"4 - { len(i) }")

# 5s
i = []
for c1 in range(0, 16):
    for c2 in range(c1 + 1, 16):
        for c3 in range(c2 + 1, 16):
            for c4 in range(c3 + 1, 16):
                for c5 in range(c4 + 1, 16):
                    c = [c1, c2, c3, c4, c5]
                    if c not in i:
                        i.append(c)
writeI(i)
print(f"5 - { len(i) }")
