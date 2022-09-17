.data
iArraySize: .word 10
iArray: .word 12, 32, 13, 43, 17, 1, -2, -45, 0, 11
numResult: .asciiz "Minimum number: "
idxResult: .asciiz "\nIndex: "
.text

main:
### StartCodeHere:
#   min		$s0
#   minIdx		$s1
#   *p		$s2
#   *end		$s3
#   index		$s4

    la      $s2,iArray             # p = iArray	
    addi    $s3,$s2,40             # end = lenArray + 10
    add     $s4,$zero,$zero        # index = 0
    add     $s1,$zero,$zero        # minIdx = 0
    lw      $s0,0($s2)             # min = iArray[0]

loop:
    beq     $s2,$s3,result	# if (p == end) goto result

    lw      $t0,0($s2)              # $t0 = *p
    addi    $s2,$s2,4               # p++

    addi     $s4,$s4,1		# index += 1

    slt     $t2,$s0,$t0             # *p < min?
    bne     $t2,$zero,loop	# no, loop

    move    $s0,$t0                 # set new min value
    addi     $s1,$s4,-1             # set new min value index
    j       loop

result:
### EndCodeHere:

# print your results
la $a0, numResult
li $v0, 4
syscall
move $a0, $s0	# get min value
li $v0, 1
syscall

la $a0, idxResult
li $v0, 4
syscall
move $a0, $s1	# get index value
li $v0, 1
syscall

#stop program
li $v0, 10
syscall

#your function start from here