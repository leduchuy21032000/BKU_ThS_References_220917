.data
cSpace:                .asciiz " " 
cEndLine:            .asciiz "\n" 
iArraySize:          .word 10 
iArray:                 .word 12, 32, 13, 43, 17, 1, -2, -45, 0, 11

.text
main:
# print integer array
lw    $t0, iArraySize # load size of iArray
la     $t1, iArray # Load base address of iArray
jal    print
# StartCodeHere
la     $s0, iArray # reload base address of iArray to register s0, left all register $t for calculating and all register $s for storing base address
lw     $t0, iArraySize # reload size of iArray because $t0 was changed after print function
# Call function quicksort(arr, 0, 9) like code in C++
addi	$a0, $s0, 0		# argument 1 is arr*
addi 	$a1,$0, 0		# argument 2 is left (= 0)
addi 	$a2, $t0, -1		# argument 3 is right (= 10 - 1 = 9)
jal 	QUICKSORT	# function QUICKSORT will be implemented below
# EndCode Here
# print integer array
lw      $t0, iArraySize # load size of iArray
la       $t1, iArray # load base address of iArray
jal      print 

# stop program
li        $v0, 10 
syscall 

print: # print function
add    $t2, $0, $0 # index of iArray
loopPrint: 
beqz   $t0, exitPrint # Check condition
li        $v0, 1 # service 1 is print integer
add    $t3, $t1, $t2 # load desired value into $a0
lw      $a0, ($t3) 
syscall 

li        $v0, 4 
la       $a0, cSpace # print space just like separator
syscall 

addi   $t0, $t0, -1 # decrease loop count
addi   $t2, $t2, 4 # increase index
b        loopPrint 
exitPrint: 
li        $v0, 4 
la       $a0, cEndLine # print endline
syscall 
jr        $ra # end of print
# my function start from here

# Firstly, quickSort function
QUICKSORT:
addi 	$sp, $sp, -20		# adjust stack for 5 items
sw 	$ra, 16($sp)		#save return address
sw 	$a0, 12($sp)		#save a0
sw 	$a1, 8($sp)		#save a1
sw 	$a2, 4($sp)		#save a2
sw 	$s0 , 0($sp)		#save $s0 -> call partition function	

# if (left < right)
sub 	$t1, $a1, $a2		# t1 = left - right
bgez 	$t1, quicksort_done 	# if left >= right -> quicksort_done
jal	partition	           # call partition function

move 	$s0, $v1		# assign output of partition function to $s0
addi	$a2, $s0, -1		# a2 = j - 1
jal 	QUICKSORT	# recursive call quickSort(arr,left,j-1)

addi 	$a1, $s0, 1		# a1 = j + 1
lw 	$a2, 4($sp)		# get the origin value of a2
jal 	QUICKSORT	# recursive call quicksort(arr,j+1,right)

# return
quicksort_done:
lw 	$s0, 0($sp)
lw 	$a2, 4($sp)
lw 	$a1, 8($sp)
lw 	$a0, 12($sp)
lw 	$ra, 16($sp)
addi	$sp, $sp, 20 
jr 	$ra  # end of quicksort function

# Secondly, partition function
partition:
addi	$sp, $sp, -4		#adjust stack for 1 item
sw	$s1, 0($sp)
	
# assign left,right,pivot
move	$t3, $a1		# t3 = a1 = pivot = left 
move	$t4, $a2		# t4 = a2 = j = right
addi	$t5, $t3, 1		# t5 = i = left + 1
la	$s0, iArray

while_loop_1:
# if (i > j) or arr[i] >= arr[pivot] -> while_loop_2
# Check condition arr[i] >= arr[pivot]
sll	$t6, $t5, 2		# t6 = 4*i
add	$s2, $s0, $t6		# s2 is base address of arr[i]
sll	$t7, $t3, 2		# t7 = 4*pivot
add	$s3, $s0, $t7		# s3 is base address of arr[pivot]
lw	$t6, 0($s2)		# t6 = arr[i]
lw	$t7, 0($s3)		# t7 = arr[pivot]
sub	$t6, $t6, $t7		# t6 = arr[i] - arr[pivot]
bgez	$t6, while_loop_2
# Check condition i > j
sub	$t6, $t5, $t4		#t6 = i - j
bgtz	$t6, while_loop_2	
# else -> i++ -> while_loop_1
addi	$t5, $t5, 1
j	while_loop_1

while_loop_2:
# if arr[j] < arr[pivot] or (i > j) -> while_loop_2_done
# Check condition arr[j] < arr[pivot]
sll	$t6, $t4, 2		# t6 = 4*j
add	$s2, $s0, $t6		# s2 is base address of arr[j]
sll	$t7, $t3,2		# t7 = 4*pivot
add	$s3, $s0, $t7		# s3 is base address of arr[pivot]
lw	$t6, 0($s2)		# t6 = arr[j]
lw	$t7, 0($s3)		# t7 = arr[pivot]
sub	$t6, $t6, $t7		# t6 = arr[j] - arr[pivot]
bltz	$t6, while_loop_2_done
# Check condition i > j
sub	$t6, $t5, $t4		#t6 = i - j
bgtz	$t6, while_loop_2_done
# else -> j-- -> while_loop_2
addi	$t4, $t4, -1
j	while_loop_2
while_loop_2_done:
# if i > j -> while_loop_done
sub	$t6, $t5, $t4		# t6 = i - j
bgtz	$t6, while_loop_done	
# else swap(arr,i,j) and -> while_loop_1
sll	$t6, $t5, 2		# t6 = 4*i
sll	$t7, $t4, 2		# t7 = 4*j
add	$s2, $s0, $t6		# s2 is base address of arr[i]
add	$s3, $s0, $t7		# s3 is base address of arr[j]
lw	$t6, 0($s2)		# t6 = arr[i]
lw	$t7, 0($s3)		# t7 = arr[j]
sw	$t7, 0($s2)		# arr[i] <- arr[j]
sw	$t6, 0($s3)		# arr[j] <- arr[i]
addi	$t5, $t5, 1		# i++
addi	$t4, $t4, -1		# j--
j	while_loop_1

while_loop_done:
# swap(arr,left,j)
sll	$t6, $t3, 2		# t6 = 4*left
sll	$t7, $t4, 2		# t7 = 4*j
add	$s2, $s0, $t6		# s2 is base address of arr[left]
add	$s3, $s0, $t7		# s3 is base address of arr[j]
lw	$t6, 0($s2)		# t6 = arr[left]
lw	$t7, 0($s3)		# t7 = arr[j]
sw	$t7, 0($s2)		# arr[left] <- arr[j]
sw	$t6, 0($s3)		# arr[j] <- arr[left]
	
# return j
move 	$s1, $t4		# maintain the value before return
add	$v1, $s1, $0		# return v1 = j
lw	$s1, 0($sp)
addi	$sp, $sp,4
jr	$ra # end of partition function

